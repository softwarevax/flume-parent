package com.softwarevax.flume.sink.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;

@Slf4j
public class MySQLSink extends AbstractJdbcSink {

    @Override
    public void configure(Context context) {
        super.configure(context);
    }

    @Override
    public Status process() throws EventDeliveryException {
        return super.process();
    }
}
