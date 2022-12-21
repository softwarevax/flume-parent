package com.softwarevax.flume.source.mysql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum  DataBaseType {

    MySql("mysql", "com.mysql.jdbc.Driver"),

    Tddl("mysql", "com.mysql.jdbc.Driver"),

    DRDS("drds", "com.mysql.jdbc.Driver"),

    Oracle("oracle", "oracle.jdbc.OracleDriver"),

    SQLServer("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    PostgreSQL("postgresql", "org.postgresql.Driver"),

    RDBMS("rdbms", "com.alibaba.datax.plugin.rdbms.util.DataBaseType"),

    DB2("db2", "com.ibm.db2.jcc.DB2Driver"),

    ADS("ads", "com.mysql.jdbc.Driver"),

    Greenplum("greenplum", "com.pivotal.jdbc.GreenplumDriver");


    private String typeName;
    private String driverClassName;
    private static Pattern mysqlPattern = Pattern.compile("jdbc:mysql://(.+):\\d+/.+");
    private static Pattern oraclePattern = Pattern.compile("jdbc:oracle:thin:@(.+):\\d+:.+");

    private DataBaseType(String typeName, String driverClassName) {
        this.typeName = typeName;
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public String appendJDBCSuffixForReader(String jdbc) {
        String result = jdbc;
        String suffix = null;
        switch(this) {
            case MySql:
            case DRDS:
                suffix = "yearIsDateType=false&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false&rewriteBatchedStatements=true";
                if (jdbc.contains("?")) {
                    result = jdbc + "&" + suffix;
                } else {
                    result = jdbc + "?" + suffix;
                }
            case Oracle:
            case SQLServer:
            case DB2:
            case PostgreSQL:
            case RDBMS:
            case Greenplum:
                return result;
            default:
                throw new RuntimeException("unsupported database type.");
        }
    }

    public String appendJDBCSuffixForWriter(String jdbc) {
        String result = jdbc;
        String suffix = null;
        switch(this) {
            case MySql:
                suffix = "yearIsDateType=false&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true&tinyInt1isBit=false";
                if (jdbc.contains("?")) {
                    result = jdbc + "&" + suffix;
                } else {
                    result = jdbc + "?" + suffix;
                }
                break;
            case DRDS:
                suffix = "yearIsDateType=false&zeroDateTimeBehavior=convertToNull";
                if (jdbc.contains("?")) {
                    result = jdbc + "&" + suffix;
                } else {
                    result = jdbc + "?" + suffix;
                }
            case Oracle:
            case SQLServer:
            case DB2:
            case PostgreSQL:
            case RDBMS:
                break;
            default:
                throw new RuntimeException("unsupported database type.");
        }

        return result;
    }

    public String formatPk(String splitPk) {
        String result = splitPk;
        switch(this) {
            case MySql:
            case Oracle:
                if (splitPk.length() >= 2 && splitPk.startsWith("`") && splitPk.endsWith("`")) {
                    result = splitPk.substring(1, splitPk.length() - 1).toLowerCase();
                }
                break;
            case DRDS:
            default:
                throw new RuntimeException("unsupported database type.");
            case SQLServer:
                if (splitPk.length() >= 2 && splitPk.startsWith("[") && splitPk.endsWith("]")) {
                    result = splitPk.substring(1, splitPk.length() - 1).toLowerCase();
                }
            case DB2:
            case PostgreSQL:
        }

        return result;
    }

    public String quoteColumnName(String columnName) {
        String result = columnName;
        switch(this) {
            case MySql:
                result = "`" + columnName.replace("`", "``") + "`";
                break;
            case DRDS:
            default:
                throw new RuntimeException("unsupported database type");
            case Oracle:
            case DB2:
            case PostgreSQL:
                break;
            case SQLServer:
                result = "[" + columnName + "]";
        }

        return result;
    }

    public String quoteTableName(String tableName) {
        String result = tableName;
        switch(this) {
            case MySql:
                result = "`" + tableName.replace("`", "``") + "`";
            case Oracle:
            case SQLServer:
            case DB2:
            case PostgreSQL:
                return result;
            case DRDS:
            default:
                throw new RuntimeException("unsupported database type");
        }
    }

    public static String parseIpFromJdbcUrl(String jdbcUrl) {
        Matcher mysql = mysqlPattern.matcher(jdbcUrl);
        if (mysql.matches()) {
            return mysql.group(1);
        } else {
            Matcher oracle = oraclePattern.matcher(jdbcUrl);
            return oracle.matches() ? oracle.group(1) : null;
        }
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
