package com.softwarevax.flume.utils;

import org.apache.flume.FlumeException;

import java.util.Collection;

public final class Assert {

    public static void isTrue(boolean expression, String message) {
        if(!expression) {
            throw new FlumeException(message);
        }
    }

    public static void notNull(Object obj, String message) {
        if(obj == null) {
            throw new FlumeException(message);
        }
    }

    public static void isBlank(String str, String message) {
        if(str != null || str.length() > 0) {
            throw new FlumeException(message);
        }
    }

    public static void isNotBlank(String str, String message) {
        if(str == null || str.length() <= 0) {
            throw new FlumeException(message);
        }
    }

    public static void isEmpty(Collection collection, String message) {
        if(collection != null || collection.size() > 0) {
            throw new FlumeException(message);
        }
    }

    public static void isNotEmpty(Collection collection, String message) {
        if(collection == null || collection.size() <= 0) {
            throw new FlumeException(message);
        }
    }
}
