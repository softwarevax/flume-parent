package com.softwarevax.flume.client.configura;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlumeClientStart implements ApplicationListener<ContextRefreshedEvent> {

    public static final String ROOT_NODE = "/flume";

    public static final String FLUME_CLIENT_NODE = "flume-client";


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

    }


}
