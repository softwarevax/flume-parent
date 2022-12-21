package com.softwarevax.flume.agent.embedded;

import com.softwarevax.flume.agent.Agent;
import com.softwarevax.flume.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Channel;
import org.apache.flume.SinkRunner;
import org.apache.flume.SourceRunner;
import org.apache.flume.conf.LogPrivacyUtil;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.lifecycle.LifecycleSupervisor;
import org.apache.flume.lifecycle.LifecycleSupervisor.SupervisorPolicy.AlwaysRestartPolicy;
import org.apache.flume.node.MaterializedConfiguration;

import java.util.*;

@Slf4j
public class EmbeddedAgent implements Agent {

    private final MaterializedConfigurationProvider configurationProvider;
    private final String name;
    private final LifecycleSupervisor supervisor;
    private EmbeddedAgent.State state;
    private List<SourceRunner> sourceRunners;
    private Channel channel;
    private SinkRunner sinkRunner;

    EmbeddedAgent(MaterializedConfigurationProvider configurationProvider, String name) {
        this.configurationProvider = configurationProvider;
        this.name = name;
        this.state = EmbeddedAgent.State.NEW;
        this.supervisor = new LifecycleSupervisor();
    }

    public EmbeddedAgent(String name) {
        this(new MaterializedConfigurationProvider(), name);
    }

    public void configure(Map<String, String> properties) {
        Assert.isTrue(this.state != EmbeddedAgent.State.STARTED, "Cannot be configured while started");
        this.doConfigure(properties);
        this.state = EmbeddedAgent.State.STOPPED;
    }

    @Override
    public EmbeddedAgent.State start() {
        Assert.isTrue(this.state != EmbeddedAgent.State.STARTED, "Cannot be started while started");
        Assert.isTrue(this.state != EmbeddedAgent.State.NEW, "Cannot be started before being configured");
        Assert.isNotEmpty(this.sourceRunners, "Source runner returned null source");
        this.doStart();
        this.state = EmbeddedAgent.State.STARTED;
        return this.state;
    }

    @Override
    public EmbeddedAgent.State stop() {
        Assert.isTrue(this.state == EmbeddedAgent.State.STARTED, "Cannot be stopped unless started");
        this.supervisor.stop();
        this.state = EmbeddedAgent.State.STOPPED;
        return this.state;
    }

    private void doConfigure(Map<String, String> properties) {
        properties = EmbeddedAgentConfiguration.configure(this.name, properties);
        if (log.isDebugEnabled() && LogPrivacyUtil.allowLogPrintConfig()) {
            log.debug("Agent configuration values");
            Iterator iterator = (new TreeSet(properties.keySet())).iterator();

            while(iterator.hasNext()) {
                String key = (String)iterator.next();
                log.debug(key + " = " + properties.get(key));
            }
        }

        MaterializedConfiguration conf = this.configurationProvider.get(this.name, properties);
        // source
        Map<String, SourceRunner> sources = conf.getSourceRunners();
        Assert.isTrue(sources.size() >= 1, "Expect at least one source and got " + sources.size());
        // channel
        Map<String, Channel> channels = conf.getChannels();
        Assert.isTrue(channels.size() == 1, "Expected one channel and got " + channels.size());
        // sink
        Map<String, SinkRunner> sinks = conf.getSinkRunners();
        Assert.isTrue(sinks.size() == 1, "Expected one sink group and got " + sinks.size());

        this.sourceRunners = new ArrayList<>(sources.values());
        this.channel = channels.values().iterator().next();
        this.sinkRunner = sinks.values().iterator().next();
    }

    private void doStart() {
        boolean error = true;

        try {
            this.channel.start();
            this.sinkRunner.start();
            this.sourceRunners.forEach(row -> row.start());

            this.supervisor.supervise(this.channel, new AlwaysRestartPolicy(), LifecycleState.START);
            this.supervisor.supervise(this.sinkRunner, new AlwaysRestartPolicy(), LifecycleState.START);
            this.sourceRunners.forEach(row -> this.supervisor.supervise(row, new AlwaysRestartPolicy(), LifecycleState.START));
            error = false;
        } finally {
            if (error) {
                this.sourceRunners.forEach(row -> this.stopLogError(row));
                this.stopLogError(this.channel);
                this.stopLogError(this.sinkRunner);
                this.supervisor.stop();
            }
        }

    }

    private void stopLogError(LifecycleAware lifeCycleAware) {
        try {
            if (LifecycleState.START.equals(lifeCycleAware.getLifecycleState())) {
                lifeCycleAware.stop();
            }
        } catch (Exception var3) {
            log.warn("Exception while stopping " + lifeCycleAware, var3);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedAgent that = (EmbeddedAgent) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static enum State {
        /**
         * 新建
         */
        NEW,

        /**
         * 已停止
         */
        STOPPED,

        /**
         * 已启动
         */
        STARTED,

        /**
         * 已提交
         */
        SUBMITTED,

        ;

        private State() {
        }
    }
}
