package com.softwarevax.flume.sink.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Slf4j
@Data
public abstract class AbstractJdbcSink extends AbstractSink implements Configurable {

    public static final String DRIVER_CLASS = "driverClassName";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String JDBC_URL = "url";

    public static final String TABLE_NAME = "table";

    public static final String COLUMNS = "columns";

    public static final String FETCH_SIZE = "batch.size";

    protected JdbcTemplate template;

    protected DataSource dataSource;

    protected Connection conn;

    protected String driverClassName;

    protected String username;

    protected String password;

    protected String url;

    protected String tableName;

    protected String[] columns;

    protected int batchSize;

    protected String insertSQL = "insert into %s(%s) values(%s)";

    @Override
    public void configure(Context context) {
        this.driverClassName = context.getString(DRIVER_CLASS);
        this.username = context.getString(USERNAME);
        this.password = context.getString(PASSWORD);
        this.url = context.getString(JDBC_URL);
        this.tableName = context.getString(TABLE_NAME);
        String columnStr = context.getString(COLUMNS);
        this.columns = columnStr.split(",");
        this.batchSize = context.getInteger(FETCH_SIZE, 1000);
        this.template = new JdbcTemplate();
        this.dataSource = this.template.initHikariDataSource(driverClassName, url, username, password);
        this.conn = this.template.getConnection(dataSource);
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = columns.length; i < size; i++) {
            sb.append("?");
            if(i != size - 1) {
                sb.append(",");
            }
        }
        insertSQL = String.format(insertSQL, this.tableName, columnStr, sb.toString());
        log.info("jdbc sink first insert sql = {}", insertSQL);
    }

    @Override
    public Status process() throws EventDeliveryException {
        //1、获取Channel并开启事务
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        transaction.begin();

        //2、从Channel中抓取数据打印到控制台
        try {
            int submitted = 0;
            conn.setAutoCommit(false);
            //抓取数据
            PreparedStatement statement = conn.prepareStatement(insertSQL);
            Event event;
            while (true) {
                event = channel.take();
                if (event != null) {
                    JSONObject obj = JSON.parseObject(event.getBody(), JSONObject.class);
                    for (int i = 1, size = columns.length; i <= size; i++) {
                        statement.setObject(i, obj.get(columns[i-1]));
                    }
                    statement.addBatch();
                    submitted++;
                }
                if(submitted == batchSize) {
                    break;
                }
            }
            statement.executeBatch();
            conn.commit();
            //提交事务
            transaction.commit();
            return Status.READY;
        } catch (Exception e) {
            transaction.rollback();
            return Status.BACKOFF;
        } finally {
            transaction.close();
        }
    }
}
