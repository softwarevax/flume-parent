package com.softwarevax.flume.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;
import org.apache.flume.interceptor.Interceptor;

import java.util.List;

@Slf4j
public class TimeStampInterceptor implements Interceptor, Configurable {

    private Context context;

    @Override
    public void initialize() {
        log.info(context.getString("name"));
    }

    @Override
    public Event intercept(Event event) {
        long timeStamp = System.currentTimeMillis();
        event.getHeaders().put("timeStamp", String.valueOf(timeStamp));
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> list) {
        for (Event event : list) {
            intercept(event);
        }
        return list;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Context context) {
        this.context = context;
    }

    public static class Builder implements Interceptor.Builder {

        private Context context;


        @Override
        public Interceptor build() {
            TimeStampInterceptor interceptor = new TimeStampInterceptor();
            interceptor.configure(context);
            return interceptor;
        }

        @Override
        public void configure(Context context) {
            this.context = context;
        }
    }
}
