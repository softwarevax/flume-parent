package com.softwarevax.flume.client.configura;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.softwarevax.flume.agent.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class FlumeEngine {

    @Autowired
    private FlumeConfiguration configuration;

    @Bean
    public AgentManager agentManager() {
        return new AgentManager();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        // 解决RestTemplate无法处理嵌套json的问题
        messageConverters.add(6, new FastJsonHttpMessageConverter());
        return restTemplate;
    }
}
