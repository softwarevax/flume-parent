package com.softwarevax.flume.source.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public abstract class AbstractJdbcSource extends AbstractSource implements Configurable, PollableSource {

    public static final String DRIVER_CLASS = "driverClassName";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String JDBC_URL = "url";

    public static final String TABLE_NAME = "table";

    public static final String COLUMNS = "columns";

    public static final String FETCH_SIZE = "fetch.size";

    protected JdbcTemplate template;

    protected DataSource dataSource;

    protected Connection conn;

    protected String driverClassName;

    protected String username;

    protected String password;

    protected String url;

    protected String tableName;

    protected String columns;

    protected int fetchSize;

    protected String QUERY_SQL = "select %s from %s limit %s, %s";

    protected int index = 0;

    @Override
    public void configure(Context context) {
        this.driverClassName = context.getString(DRIVER_CLASS);
        this.username = context.getString(USERNAME);
        this.password = context.getString(PASSWORD);
        this.url = context.getString(JDBC_URL);
        this.tableName = context.getString(TABLE_NAME);
        this.columns = context.getString(COLUMNS, "*");
        this.fetchSize = context.getInteger(FETCH_SIZE, 1000);
        this.template = new JdbcTemplate();
        this.dataSource = this.template.initHikariDataSource(driverClassName, url, username, password);
        this.conn = this.template.getConnection(dataSource);
        String sql = String.format(QUERY_SQL, columns, this.tableName, index, fetchSize);
        log.info("jdbc source first query sql = {}", sql);
    }

    @Override
    public Status process() throws EventDeliveryException {
        try {
            String sql = String.format(QUERY_SQL, columns, this.tableName, index, fetchSize);
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            ResultSetMetaData meta = result.getMetaData();
            int columnCount = meta.getColumnCount();

            List<Event> events = new ArrayList<>();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                SimpleEvent event = new SimpleEvent();
                for (int i = 1; i <= columnCount; i++) {
                    String column = meta.getColumnName(i).trim();
                    Object value = result.getObject(i);
                    row.put(column, value);
                }
                event.setBody(JSON.toJSONBytes(row, SerializerFeature.DisableCircularReferenceDetect));
                events.add(event);
            }
            index += fetchSize;
            getChannelProcessor().processEventBatch(events);
            //Thread.sleep(getBackOffSleepIncrement());
        } catch (Exception e) {
            return Status.BACKOFF;
        }
        return Status.READY;
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 1000;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 1000;
    }
}
