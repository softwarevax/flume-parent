package com.softwarevax.flume.exception;

import org.apache.flume.FlumeException;

public enum ExceptionEnum {

    FlumeException(FlumeException.class, 510),
    Exception(Exception.class, 500)
    ;

    private Class<?> clazz;

    private int code;

    ExceptionEnum(Class<?> clazz, int code) {
        this.clazz = clazz;
        this.code = code;
    }

    public static int getCode(Class<?> clazz) {
        if(clazz == FlumeException.clazz) {
            return FlumeException.code;
        }
        if(clazz == Exception.clazz) {
            return Exception.code;
        }
        return 0;
    }
}
