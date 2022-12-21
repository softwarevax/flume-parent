package com.softwarevax.flume.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;
import org.apache.flume.interceptor.Interceptor;

import java.util.List;
import java.util.Map;

@Slf4j
public class HeadTagInterceptor implements Interceptor, Configurable {

    public static final String HEAD_TAG = "softwarevax";

    private Context context;

    @Override
    public void initialize() {
    }

    @Override
    public Event intercept(Event event) {
        Map<String, String> headers = event.getHeaders();
        headers.put("author", HEAD_TAG);
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
            HeadTagInterceptor interceptor = new HeadTagInterceptor();
            interceptor.configure(context);
            return interceptor;
        }

        @Override
        public void configure(Context context) {
            this.context = context;
        }
    }
}
