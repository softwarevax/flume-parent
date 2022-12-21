package com.softwarevax.flume.agent.entity.channel;

import com.softwarevax.flume.agent.AgentManager;
import com.softwarevax.flume.agent.entity.Configuration;
import com.softwarevax.flume.utils.Assert;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.Data;
import org.apache.flume.conf.channel.ChannelType;

import java.util.Map;

@Data
public class AgentChannel implements Configuration {

    private String name;

    private ChannelType type;

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
        this.configuration = CommonUtils.kickOutKeyPrefix(configuration, AgentManager.CHANNEL);
        Assert.isTrue(this.configuration.containsKey("type"), "channel缺少type属性");
        this.type = selector(this.configuration.get("type"));
        Assert.isTrue(this.configuration.containsKey("capacity"), "channel缺少capacity属性");
    }

    private ChannelType selector(String type) {
        Assert.notNull(type, "channel不能为空");
        switch (type.toLowerCase()) {
            case "file":
                return ChannelType.FILE;
            case "jdbc":
                return ChannelType.JDBC;
            case "spillablememory":
                return ChannelType.SPILLABLEMEMORY;
            default:
                return ChannelType.MEMORY;
        }
    }
}
