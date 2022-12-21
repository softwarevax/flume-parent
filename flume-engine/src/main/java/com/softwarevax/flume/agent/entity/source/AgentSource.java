package com.softwarevax.flume.agent.entity.source;

import com.softwarevax.flume.agent.entity.Configuration;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentSource implements Configuration {

    private String name;

    private List<Source> sources;

    /**
     * 所有的source属性
     */
    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
        sources = CommonUtils.disperse(this, Source.class);
    }
}
