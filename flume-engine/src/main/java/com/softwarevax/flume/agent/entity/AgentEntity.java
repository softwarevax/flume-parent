package com.softwarevax.flume.agent.entity;

import com.softwarevax.flume.agent.entity.channel.AgentChannel;
import com.softwarevax.flume.agent.entity.processor.AgentProcessor;
import com.softwarevax.flume.agent.entity.sink.AgentSink;
import com.softwarevax.flume.agent.entity.source.AgentSource;

public class AgentEntity {

    /**
     * agent名称
     */
    private String name;

    /**
     * 运行的主机：ip:port
     */
    private String attachTo;

    /**
     * agent的source属性
     */
    private AgentSource source;

    /**
     * source通过channel将数据传输到sink，只支持一个
     */
    private AgentChannel channel;

    /**
     * agent的processor，数据在进入sink前，需经过processor处理
     */
    private AgentProcessor processor;

    /**
     * agent的sink属性
     */
    private AgentSink sink;

    public AgentEntity() {}

    public AgentEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttachTo() {
        return attachTo;
    }

    public void setAttachTo(String attachTo) {
        this.attachTo = attachTo;
    }

    public AgentSource getSource() {
        return source;
    }

    public AgentEntity setSource(AgentSource source) {
        this.source = source;
        return this;
    }

    public AgentChannel getChannel() {
        return channel;
    }

    public AgentEntity setChannel(AgentChannel channel) {
        this.channel = channel;
        return this;
    }

    public AgentProcessor getProcessor() {
        return processor;
    }

    public AgentEntity setProcessor(AgentProcessor processor) {
        this.processor = processor;
        return this;
    }

    public AgentSink getSink() {
        return sink;
    }

    public AgentEntity setSink(AgentSink sink) {
        this.sink = sink;
        return this;
    }
}
