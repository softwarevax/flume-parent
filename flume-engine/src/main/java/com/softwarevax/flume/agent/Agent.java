package com.softwarevax.flume.agent;

import com.softwarevax.flume.agent.embedded.EmbeddedAgent;
import org.apache.flume.FlumeException;

public interface Agent {

    EmbeddedAgent.State start() throws FlumeException;

    EmbeddedAgent.State stop() throws FlumeException;

    String getName();
}
