package com.softwarevax.flume.source.mysql;

public enum  DataBaseType {

    MySql("mysql", "com.mysql.jdbc.Driver"),

    Oracle("oracle", "oracle.jdbc.OracleDriver"),

    SQLServer("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    PostgreSQL("postgresql", "org.postgresql.Driver"),

    Greenplum("greenplum", "com.pivotal.jdbc.GreenplumDriver");


    private String typeName;
    private String driverClassName;

    DataBaseType(String typeName, String driverClassName) {
        this.typeName = typeName;
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }
}
