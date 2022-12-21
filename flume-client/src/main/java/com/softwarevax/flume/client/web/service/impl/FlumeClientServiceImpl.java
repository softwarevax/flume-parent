package com.softwarevax.flume.client.web.service.impl;

import com.softwarevax.flume.agent.AgentManager;
import com.softwarevax.flume.agent.embedded.EmbeddedAgent;
import com.softwarevax.flume.agent.entity.AgentEntity;
import com.softwarevax.flume.client.configura.Action;
import com.softwarevax.flume.client.configura.FlumeConfiguration;
import com.softwarevax.flume.client.web.service.FlumeClientService;
import com.softwarevax.flume.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FlumeClientServiceImpl implements FlumeClientService {

    @Autowired
    private Environment environment;

    @Autowired
    private AgentManager manager;

    @Autowired
    private FlumeConfiguration configuration;

    @Autowired
    private RestTemplate template;

    @Override
    public String submit(AgentEntity entity) {
        String attachTo = entity.getAttachTo();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = configuration.getProtocol() + "://" + CommonUtils.duplicate(attachTo + Action.START.url(), "/");
        HttpEntity<AgentEntity> httpEntity = new HttpEntity<>(entity, headers);
        String body = template.postForObject(url, httpEntity, String.class);
        return body;
    }

    @Override
    public String start(AgentEntity entity) {
        EmbeddedAgent.State state = manager.submit(entity);
        return state.name();
    }

    @Override
    public String stop(String agentName) {
        EmbeddedAgent.State state = manager.stop(agentName);
        return state.name();
    }
}
