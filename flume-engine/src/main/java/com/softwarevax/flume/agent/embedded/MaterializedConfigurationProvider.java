package com.softwarevax.flume.agent.embedded;



import org.apache.flume.node.MaterializedConfiguration;

import java.util.Map;

class MaterializedConfigurationProvider {
    MaterializedConfigurationProvider() {
    }

    MaterializedConfiguration get(String name, Map<String, String> properties) {
        MemoryConfigurationProvider confProvider = new MemoryConfigurationProvider(name, properties);
        return confProvider.getConfiguration();
    }
}
