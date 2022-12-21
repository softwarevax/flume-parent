package com.softwarevax.flume.agent.embedded;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.softwarevax.flume.utils.Assert;
import com.softwarevax.flume.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.channel.ChannelType;
import org.apache.flume.conf.sink.SinkProcessorType;
import org.apache.flume.conf.sink.SinkType;

import java.util.*;

@Slf4j
public class EmbeddedAgentConfiguration {
    private static final String SEPERATOR = ".";
    private static final Joiner JOINER = Joiner.on(".");
    private static final String TYPE = "type";
    public static final String SOURCE = "source";
    public static final String CHANNEL = "channel";
    public static final String SINK_PROCESSOR = "processor";
    public static final String INTERCEPTOR = "interceptor";
    public static final String SINKS = "sinks";
    public static final String SINKS_PREFIX = join("sinks", "");
    public static final String SOURCE_TYPE = join("source", "type");
    public static final String SOURCE_PREFIX = join("source", "");
    public static final String CHANNEL_TYPE = join("channel", "type");
    public static final String CHANNEL_PREFIX = join("channel", "");
    public static final String SINK_PROCESSOR_TYPE = join("processor", "type");
    public static final String SINK_PROCESSOR_PREFIX = join("processor", "");
    public static final String INTERCEPTOR_PREFIX = "interceptor_";
    public static final String CHANNEL_TYPE_MEMORY;
    public static final String CHANNEL_TYPE_SPILLABLEMEMORY;
    public static final String CHANNEL_TYPE_FILE;
    public static final String SINK_TYPE_AVRO;
    public static final String SINK_TYPE_LOGGER;
    public static final String SINK_PROCESSOR_TYPE_DEFAULT;
    public static final String SINK_PROCESSOR_TYPE_FAILOVER;
    public static final String SINK_PROCESSOR_TYPE_LOAD_BALANCE;
    private static final String[] ALLOWED_CHANNELS;
    private static final String[] ALLOWED_SINKS;
    private static final String[] ALLOWED_SINK_PROCESSORS;
    private static final ImmutableList<String> DISALLOWED_SINK_NAMES;
    // <interceptor[1], <type, "">>
    private static final Map<String, Map<String, String>> INTERCEPTORS;

    static Map<String, String> configure(String name, Map<String, String> props) throws FlumeException {
        Map<String, String> properties = new HashMap(props);

        String strippedName = name.replaceAll("\\s+", "");
        String sourceNames = properties.remove("sources");
        String channelName = strippedName + "-channel";
        String sinkNames = properties.remove("sinks");
        String sinkGroupName = strippedName + "-sink-group";

        Map<String, String> result = Maps.newHashMap();
        result.put(join(name, "sources"), sourceNames);
        result.put(join(name, "channels"), channelName);
        result.put(join(name, "sinks"), sinkNames);
        result.put(join(name, "sinkgroups"), sinkGroupName);
        result.put(join(name, "sinkgroups", sinkGroupName, "sinks"), sinkNames);

        Set<String> userProvidedKeys = new HashSet(properties.keySet());
        // source[支持多source]
        String[] sourceArray = sourceNames.split("\\s+");
        int sourceSize = sourceArray.length;

        for(int i = 0; i < sourceSize; ++i) {
            String source = sourceArray[i];
            // 拦截器名称
            StringBuilder interceptorNames = new StringBuilder();
            // 当前source所有的拦截器
            Map<String, Map<String, String>> interceptorMap = new TreeMap<>(new CommonUtils.MapKeyComparator<>());
            // 公共的拦截器(复制一份公用的拦截器，防止被用户传入的覆盖)
            interceptorMap.putAll(CommonUtils.copy(INTERCEPTORS));
            if (StringUtils.isEmpty(source)) {
                continue;
            }
            // source和interceptor
            Iterator<String> iterator = userProvidedKeys.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = properties.get(key);
                // interceptor
                String interceptorPrefix = join(source, "interceptors");
                if(key.startsWith(interceptorPrefix)) {
                    properties.remove(key);
                    // interceptor[1]
                    String interceptorKey = CommonUtils.cutOut(key, ".", ".");
                    if(!interceptorMap.containsKey(interceptorKey)) {
                        interceptorMap.put(interceptorKey, new HashMap<>());
                    }
                    Map<String, String> interceptorProps = interceptorMap.get(interceptorKey);
                    String propKey = key.substring(key.indexOf(interceptorKey) + interceptorKey.length() + 1);
                    interceptorProps.put(propKey, value);
                    continue;
                }
                // source
                if (key.startsWith(source + ".")) {
                    properties.remove(key);
                    result.put(join(name, "sources", key), value);
                }
            }
            result.put(join(name, "sources", source, "channels"), channelName);

            Iterator<Map.Entry<String, Map<String, String>>> interceptorIterator = interceptorMap.entrySet().iterator();
            while (interceptorIterator.hasNext()) {
                Map.Entry<String, Map<String, String>> interceptor = interceptorIterator.next();
                String interceptorName = source + "_" + INTERCEPTOR_PREFIX + CommonUtils.cutOut(interceptor.getKey(), "[", "]");
                interceptorNames.append(interceptorName).append(" ");
                String interceptorPropPrefix = join(name, "sources", source, "interceptors", interceptorName);
                Map<String, String> prop = interceptor.getValue();
                Iterator<Map.Entry<String, String>> propIterator = prop.entrySet().iterator();
                while (propIterator.hasNext()) {
                    Map.Entry<String, String> propVal = propIterator.next();
                    // 设置拦截器属性， eg: agent.sources.r1.interceptors.r1_interceptor_0.type=com.softwarevax.flume.interceptor.HeaderTagInterceptor$Builder
                    result.put(join(interceptorPropPrefix, propVal.getKey()), propVal.getValue());
                }
            }
            // 设置interceptors， eg: agent.sources.r1.interceptors=r1_interceptor_0 r1_interceptor_1
            String interceptorName = interceptorNames.toString().trim();
            if (StringUtils.isNotBlank(interceptorName)) {
                result.put(join(name, "sources", source, "interceptors"), interceptorName);
            }
        }

        // sinks
        String[] sinkArray = sinkNames.split("\\s+");
        int sinkSize = sinkArray.length;
        for(int i = 0; i < sinkSize; ++i) {
            String sink = sinkArray[i];
            if (!StringUtils.isEmpty(sink)) {
                Iterator iterator = userProvidedKeys.iterator();
                while(iterator.hasNext()) {
                    String key = (String)iterator.next();
                    String value = properties.get(key);
                    if (key.startsWith(sink + ".")) {
                        properties.remove(key);
                        result.put(join(name, "sinks", key), value);
                    }
                }
                result.put(join(name, "sinks", sink, "channel"), channelName);
            }
        }

        userProvidedKeys = new HashSet(properties.keySet());
        Iterator iterator = userProvidedKeys.iterator();

        while(iterator.hasNext()) {
            String key = (String)iterator.next();
            String value = properties.get(key);
            if (key.startsWith(CHANNEL_PREFIX)) {
                // channel
                key = key.replaceFirst("channel", channelName);
                result.put(join(name, "channels", key), value);
            } else {
                // processor
                Assert.isTrue(key.startsWith(SINK_PROCESSOR_PREFIX), "Unknown configuration " + key);
                result.put(join(name, "sinkgroups", sinkGroupName, key), value);
            }
        }

        String printString = CommonUtils.toString(result);
        log.info("Flume Configurations:   \n{}", printString);
        return result;
    }

    private static String join(String... parts) {
        return JOINER.join(parts);
    }

    private EmbeddedAgentConfiguration() {

    }

    static {
        CHANNEL_TYPE_MEMORY = ChannelType.MEMORY.name();
        CHANNEL_TYPE_SPILLABLEMEMORY = ChannelType.SPILLABLEMEMORY.name();
        CHANNEL_TYPE_FILE = ChannelType.FILE.name();
        SINK_TYPE_AVRO = SinkType.AVRO.name();
        SINK_TYPE_LOGGER = SinkType.LOGGER.name();
        SINK_PROCESSOR_TYPE_DEFAULT = SinkProcessorType.DEFAULT.name();
        SINK_PROCESSOR_TYPE_FAILOVER = SinkProcessorType.FAILOVER.name();
        SINK_PROCESSOR_TYPE_LOAD_BALANCE = SinkProcessorType.LOAD_BALANCE.name();
        ALLOWED_CHANNELS = new String[]{CHANNEL_TYPE_MEMORY, CHANNEL_TYPE_FILE};
        ALLOWED_SINKS = new String[]{SINK_TYPE_AVRO, SINK_TYPE_LOGGER};
        ALLOWED_SINK_PROCESSORS = new String[]{SINK_PROCESSOR_TYPE_DEFAULT, SINK_PROCESSOR_TYPE_FAILOVER, SINK_PROCESSOR_TYPE_LOAD_BALANCE};
        DISALLOWED_SINK_NAMES = ImmutableList.of("source", "channel", "processor");
        // 初始化interceptor
        INTERCEPTORS = new HashMap<>();
        Map<String, String> prop = new HashMap<>();
        // type属性
        prop.put("type", "com.softwarevax.flume.interceptor.HeadTagInterceptor$Builder");
        // 放在第一位
        INTERCEPTORS.put("interceptors[0]", prop);
    }
}
