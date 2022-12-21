package com.softwarevax.flume.client.web.service;

import com.softwarevax.flume.agent.entity.AgentEntity;

public interface FlumeClientService {

    String submit(AgentEntity entity);

    String start(AgentEntity entity);

    String stop(String agentName);

}
