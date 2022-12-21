package com.softwarevax.flume.agent.entity.sink;

import com.softwarevax.flume.agent.entity.Configuration;
import lombok.Data;

import java.util.Map;

@Data
public class Sink implements Configuration {

    private String name;

    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
