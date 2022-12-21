package com.softwarevax.flume.source.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;

@Slf4j
public class MySQLSource extends AbstractJdbcSource {

    @Override
    public void configure(Context context) {
        super.configure(context);
    }

    @Override
    public Status process() throws EventDeliveryException {
        return super.process();
    }
}
