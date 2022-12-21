package com.softwarevax.flume.sink.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JdbcTemplate {

    public static final int TIMEOUT_SECONDS = 15;

    public JdbcTemplate() {
    }

    public HikariDataSource initHikariDataSource(String driverClassName, String jdbcUrl, String username, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(500);
        dataSource.setMinimumIdle(5);
        dataSource.setLeakDetectionThreshold(15000L);
        if (driverClassName.indexOf("oracle") != -1) {
            dataSource.setConnectionTestQuery("SELECT 1 FROM DUAL");
        } else {
            dataSource.setConnectionTestQuery("SELECT 1");
        }

        dataSource.setConnectionTimeout(30000L);
        dataSource.setIdleTimeout(600000L);
        dataSource.setLeakDetectionThreshold(TimeUnit.MINUTES.toMillis(1L));
        return dataSource;
    }

    public synchronized Connection connect(DataBaseType dataBaseType, String url, String user, String pass, String socketTimeout) {
        Properties prop = new Properties();
        prop.put("user", user);
        prop.put("password", pass);
        if (dataBaseType == DataBaseType.Oracle) {
            prop.put("oracle.jdbc.ReadTimeout", socketTimeout);
        }

        return connect(dataBaseType, url, prop);
    }

    private synchronized Connection connect(DataBaseType dataBaseType, String url, Properties prop) {
        try {
            Class.forName(dataBaseType.getDriverClassName());
            DriverManager.setLoginTimeout(15);
            return DriverManager.getConnection(url, prop);
        } catch (Exception var4) {
            throw new RuntimeException(prop.getProperty("user"), var4);
        }
    }

    public Connection getConnection(DataSource dataSource) {
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

        return conn;
    }

    public ResultSet query(Statement stmt, Connection conn, String sql, int fetchSize, int queryTimeout) throws SQLException {
        stmt.setFetchSize(fetchSize);
        stmt.setQueryTimeout(queryTimeout);
        conn.setAutoCommit(false);
        return query(stmt, sql);
    }

    public ResultSet query(Statement stmt, String sql) throws SQLException {
        return stmt.executeQuery(sql);
    }

    public void closeDBResources(Connection connection, Statement stmt, ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException var6) {
            }
        }

        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException var5) {
            }
        }

        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException var4) {
            }
        }

    }
}
