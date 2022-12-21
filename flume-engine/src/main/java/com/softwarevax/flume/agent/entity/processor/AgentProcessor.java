package com.softwarevax.flume.agent.entity.processor;

import com.softwarevax.flume.agent.AgentManager;
import com.softwarevax.flume.agent.entity.Configuration;
import com.softwarevax.flume.utils.Assert;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.Data;
import org.apache.flume.conf.sink.SinkProcessorType;

import java.util.Map;

@Data
public class AgentProcessor implements Configuration {

    private String name;

    private SinkProcessorType type;

    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        if(type != null) {
            configuration.put("type", type.name());
        }
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = CommonUtils.kickOutKeyPrefix(configuration, AgentManager.PROCESSOR);
        Assert.isTrue(this.configuration.containsKey("type"), "processor缺少type属性");
        this.type = selector(this.configuration.get("type"));
    }

    private SinkProcessorType selector(String type) {
        Assert.notNull(type, "channel不能为空");
        switch (type.toLowerCase()) {
            case "failover":
                return SinkProcessorType.FAILOVER;
            case "load_balance":
                return SinkProcessorType.LOAD_BALANCE;
            default:
                return SinkProcessorType.DEFAULT;
        }
    }
}
