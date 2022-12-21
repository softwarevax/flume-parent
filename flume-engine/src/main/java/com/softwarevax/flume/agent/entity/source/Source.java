package com.softwarevax.flume.agent.entity.source;

import com.softwarevax.flume.agent.AgentManager;
import com.softwarevax.flume.agent.embedded.EmbeddedAgentConfiguration;
import com.softwarevax.flume.agent.entity.Configuration;
import com.softwarevax.flume.agent.entity.interceptor.AgentInterceptor;
import com.softwarevax.flume.utils.Assert;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.Data;

import java.util.*;

@Data
public class Source implements Configuration {

    private String name;

    private List<AgentInterceptor> interceptors;

    private Map<String, String> configuration;

    @Override
    public Map<String, String> getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
        this.interceptors = new ArrayList<>();
        Map<String, String> interceptorMaps = CommonUtils.filterByPrefix(configuration, Arrays.asList(AgentManager.INTERCEPTOR));
        Iterator<Map.Entry<String, String>> iterator = interceptorMaps.entrySet().iterator();
        Map<String, Map<String, String>> props = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String name = EmbeddedAgentConfiguration.INTERCEPTOR_PREFIX + CommonUtils.cutOut(key, "[", "]");
            if(!props.containsKey(name)) {
                props.put(name, new HashMap<>());
            }
            Map<String, String> vals = props.get(name);
            String replace = key.substring(0, key.indexOf(".") + 1);
            vals.put(key.replace(replace, ""), entry.getValue());
        }
        Iterator<Map.Entry<String, Map<String, String>>> interceptorIterator = props.entrySet().iterator();
        while (interceptorIterator.hasNext()) {
            Map.Entry<String, Map<String, String>> entry = interceptorIterator.next();
            AgentInterceptor interceptor = new AgentInterceptor();
            interceptor.setName(entry.getKey());
            Map<String, String> value = entry.getValue();
            Assert.isTrue(value.containsKey("type"), "拦截器缺少type属性");
            interceptor.setType(value.get("type"));
            interceptor.setConfiguration(value);
            interceptors.add(interceptor);
        }
    }
}
