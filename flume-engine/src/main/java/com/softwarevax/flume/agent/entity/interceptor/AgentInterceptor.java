package com.softwarevax.flume.agent.entity.interceptor;

import com.softwarevax.flume.agent.entity.Configuration;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

@Data
public class AgentInterceptor implements Configuration {

    /**
     * 格式interceptor_0，必填
     */
    private String name;

    private String type;

    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        if(StringUtils.isNotBlank(type)) {
            configuration.put("type", type);
        }
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
