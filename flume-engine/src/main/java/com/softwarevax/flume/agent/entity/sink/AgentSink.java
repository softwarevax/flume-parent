package com.softwarevax.flume.agent.entity.sink;

import com.softwarevax.flume.agent.entity.Configuration;
import com.softwarevax.flume.agent.entity.source.Source;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentSink implements Configuration {

    private String name;

    private List<Sink> sinks;

    /**
     * 所有sink的属性
     */
    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
        sinks = CommonUtils.disperse(this, Sink.class);
    }
}

