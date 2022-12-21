package com.softwarevax.flume.agent;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.softwarevax.flume.agent.embedded.EmbeddedAgent;
import com.softwarevax.flume.agent.entity.AgentEntity;
import com.softwarevax.flume.agent.entity.channel.AgentChannel;
import com.softwarevax.flume.agent.entity.interceptor.AgentInterceptor;
import com.softwarevax.flume.agent.entity.processor.AgentProcessor;
import com.softwarevax.flume.agent.entity.sink.AgentSink;
import com.softwarevax.flume.agent.entity.sink.Sink;
import com.softwarevax.flume.agent.entity.source.AgentSource;
import com.softwarevax.flume.agent.entity.source.Source;
import com.softwarevax.flume.utils.Assert;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.channel.ChannelType;
import org.apache.flume.conf.sink.SinkProcessorType;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class AgentManager {

    public static final String SOURCE_GROUP = "sources";
    public static final String SINK_GROUP = "sinks";
    public static final String CHANNEL = "channel";
    public static final String PROCESSOR = "processor";
    public static final String INTERCEPTOR = "interceptor";
    public static final String CANNEL_MEMORY_CAPACITY = "100000";
    public static final String TRANSACTION_CAPACITY = "1000";

    /**
     * 正在运行的agent
     */
    private Map<String, Agent> runningAgents;

    /**
     * 未运行的agent
     */
    private LinkedBlockingDeque<Agent> silentAgents;

    /**
     * 线程池
     */
    public ScheduledExecutorService submitPool;

    public EmbeddedAgent.State delete(Agent name) throws FlumeException {
        return null;
    }

    public EmbeddedAgent.State submit(AgentEntity entity) throws FlumeException {
        List<String> running = new ArrayList<>(this.runningAgents.keySet());
        if(running.contains(entity.getName())) {
            return EmbeddedAgent.State.STARTED;
        }
        List<String> submitted = this.silentAgents.stream().map(Agent::getName).collect(Collectors.toList());
        if(submitted.contains(entity.getName())) {
            // 如果还未启动，则允许替换更新的任务
            this.silentAgents.remove(new EmbeddedAgent(entity.getName()));
        }
        EmbeddedAgent agent = new EmbeddedAgent(entity.getName());
        Map<String, String> configure = configure(entity);
        agent.configure(configure);
        this.silentAgents.add(agent);
        return EmbeddedAgent.State.SUBMITTED;
    }

    public EmbeddedAgent.State stop(String name) throws FlumeException {
        Assert.isTrue(this.runningAgents.containsKey(name), "没有正在运行的agent[" + name + "]");
        Agent agent = this.runningAgents.get(name);
        return agent.stop();
    }

    public void close() {
        Iterator<Agent> iterator = this.runningAgents.values().iterator();
        while (iterator.hasNext()) {
            Agent agent = iterator.next();
            agent.stop();
        }
    }

    public AgentManager() {
        this.runningAgents = new HashMap<>();
        this.silentAgents = new LinkedBlockingDeque<>();
        this.submitPool = Executors.newSingleThreadScheduledExecutor((new ThreadFactoryBuilder()).setNameFormat("Task-Submit-Pool").build());

        this.submitPool.scheduleAtFixedRate(() -> {
            try {
                Agent agent = this.silentAgents.take();
                agent.start();
                this.runningAgents.put(agent.getName(), agent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    public AgentEntity configure(String name, Map<String, String> props) {
        Map<String, String> properties = CommonUtils.copy(props);
        Assert.isTrue(properties.containsKey(SOURCE_GROUP), "缺少配置：" + SOURCE_GROUP);
        Assert.isTrue(properties.containsKey(SINK_GROUP), "缺少配置：" + SINK_GROUP);
        String source = properties.get(SOURCE_GROUP);
        List<String> sources = CommonUtils.splitToList(source, "\\s+");
        String sink = properties.get(SINK_GROUP);
        List<String> sinks = CommonUtils.splitToList(sink, "\\s+");
        // processor给默认值
        if(!CommonUtils.containsKey(properties, PROCESSOR)) {
            SinkProcessorType type = CollectionUtils.size(sinks) > 1 ? SinkProcessorType.LOAD_BALANCE : SinkProcessorType.DEFAULT;
            properties.put(CommonUtils.join(PROCESSOR, "type"), type.name());
        }
        // channel给默认值
        if(!CommonUtils.containsKey(properties, CHANNEL)) {
            ChannelType type = ChannelType.MEMORY;
            properties.put(CommonUtils.join(CHANNEL, "type"), type.name());
            properties.put(CommonUtils.join(CHANNEL, "capacity"), CANNEL_MEMORY_CAPACITY);
        }
        List<String> prefix = CommonUtils.merge(sources, sinks);
        prefix.add(PROCESSOR);
        prefix.add(CHANNEL);
        prefix.add(SOURCE_GROUP);
        prefix.add(SINK_GROUP);

        // 有用的属性
        Map<String, String> avaliable = CommonUtils.filterByPrefix(properties, prefix);
        // source属性
        Map<String, String> sourceMaps = CommonUtils.filterByPrefix(avaliable, sources);
        Map<String, String> channelMaps = CommonUtils.filterByPrefix(avaliable, Lists.newArrayList(CHANNEL));
        Map<String, String> processorMaps = CommonUtils.filterByPrefix(avaliable, Lists.newArrayList(PROCESSOR));
        Map<String, String> sinkMaps = CommonUtils.filterByPrefix(avaliable, sinks);

        AgentSource agentSource = new AgentSource();
        agentSource.setName(source);
        agentSource.setConfiguration(sourceMaps);

        AgentChannel channel = new AgentChannel();
        channel.setConfiguration(channelMaps);
        channel.setName(name + "_" + CHANNEL);

        AgentProcessor processor = new AgentProcessor();
        processor.setConfiguration(processorMaps);
        processor.setName(name + "_" + PROCESSOR);

        AgentSink agentSink = new AgentSink();
        agentSink.setName(sink);
        agentSink.setConfiguration(sinkMaps);

        AgentEntity entity = new AgentEntity(name);
        entity.setSource(agentSource).setChannel(channel).setProcessor(processor).setSink(agentSink);
        return entity;
    }

    public Map<String, String> configure(AgentEntity entity) {
        Map<String, String> map = new HashMap<>();

        // sources interceptors
        AgentSource agentSource = entity.getSource();
        Assert.notNull(agentSource, "AgentSource不能为空");
        List<Source> sources = agentSource.getSources();
        map.put(SOURCE_GROUP, CommonUtils.join(sources.stream().map(Source::getName).collect(Collectors.toList()), " "));
        for (Source source : sources) {
            map.putAll(CommonUtils.attachKeyPrefix(source.getConfiguration(), source.getName() + "."));
            List<AgentInterceptor> interceptors = source.getInterceptors();
            for (AgentInterceptor interceptor : interceptors) {
                String interceptorPrefix = CommonUtils.join(source.getName(), interceptor.getName());
                interceptorPrefix = interceptorPrefix.replace("_", "s[") + "].";
                map.putAll(CommonUtils.attachKeyPrefix(interceptor.getConfiguration(), interceptorPrefix));
            }
        }

        // channel
        AgentChannel channel = entity.getChannel();
        Assert.notNull(channel, "Channel不能为空");
        map.putAll(CommonUtils.attachKeyPrefix(channel.getConfiguration(), CHANNEL + "."));

        // processor
        AgentProcessor processor = entity.getProcessor();
        Assert.notNull(processor, "Processor不能为空");
        map.putAll(CommonUtils.attachKeyPrefix(processor.getConfiguration(), PROCESSOR + "."));

        // sinks
        AgentSink agentSink = entity.getSink();
        Assert.notNull(agentSink, "AgentSink不能为空");
        List<Sink> sinks = agentSink.getSinks();
        map.put(SINK_GROUP, CommonUtils.join(sinks.stream().map(Sink::getName).collect(Collectors.toList()), " "));
        for (Sink sink : sinks) {
            map.putAll(CommonUtils.attachKeyPrefix(sink.getConfiguration(), sink.getName() + "."));
        }
        return map;
    }


}
