package com.softwarevax.flume.client.configura;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "flume")
public class FlumeConfiguration {

    private String protocol;

    private String master;

    /**
     * 单位：s
     */
    private int heartBeat;
}
