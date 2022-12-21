package com.softwarevax.flume.agent.entity;

import java.util.Map;

public interface Configuration {

    String getName();

    void setName(String name);

    Map<String, String> getConfiguration();

    void setConfiguration(Map<String, String> configuration);
}
