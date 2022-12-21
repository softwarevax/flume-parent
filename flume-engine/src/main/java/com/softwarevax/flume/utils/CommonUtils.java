package com.softwarevax.flume.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.softwarevax.flume.agent.entity.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

@Slf4j
public final class CommonUtils {

    public static String toString(final Map<String, String> configuration) {
        Map<String,String> maps = new TreeMap<>(new MapKeyComparator<>());
        maps.putAll(configuration);
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = maps.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            builder.append(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
        }

        return builder.toString();
    }

    public static String cutOut(String str, String first, String second) {
        Assert.isNotBlank(str, "字符串不能为空");
        Assert.isTrue(str.indexOf(first) > -1, str + "不含字符串" + first);
        int firstIdx = str.indexOf(first);
        Assert.isTrue(str.indexOf(second, firstIdx) > -1, str + "不含字符串" + second);
        int secondIdx = str.indexOf(second, firstIdx + 1);
        return str.substring(firstIdx + 1, secondIdx);
    }

    public static List<String> splitToList(String str, String regex) {
        if(str == null || str.length() == 0) {
            return new ArrayList<>();
        }
        String[] split = str.split(regex);
        return Lists.newArrayList(split);
    }

    public static Map<String, String> filterByPrefix(Map<String, String> properties, List<String> keys) {
        if(properties == null || properties.size() == 0 || keys == null || keys.size() == 0) {
            return properties;
        }
        Map<String, String> copy = copy(properties);
        Iterator<String> iterator = copy.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Optional<String> first = keys.stream().filter(row -> key.startsWith(row)).findFirst();
            if(!first.isPresent()) {
                iterator.remove();
            }
        }
        return copy;
    }

    public static Map<String, String> mapOf(String ... kv) {
        Assert.notNull(kv, "数组不能为空");
        Assert.isTrue(kv.length % 2 == 0, "键值对没有成对");
        Map<String, String> map = new HashMap<>();
        for (int i = 0, size = kv.length; i < size; i = i + 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    public static <T> List<T> merge(List<T> ... list) {
        List<T> ts = new ArrayList<>();
        for (int i = 0,  size = list.length; i < size; i++) {
            ts.addAll(list[i]);
        }
        return ts;
    }

    public static <T> List<T> copy(List<T> src) {
        try {
            ObjectInputStream ois = getInputStream(src);
            List<T> dest = (List<T>) ois.readObject();
            ois.close();
            return dest;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public static <K, V> Map<K, V> copy(Map<K, V> src) {
        try {
            ObjectInputStream ois = getInputStream(src);
            Map<K, V> result =  (Map<K, V>)ois.readObject();
            ois.close();
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private static ObjectInputStream getInputStream(Object src) throws IOException {
        //写入字节流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream obs = new ObjectOutputStream(out);
        obs.writeObject(src);
        obs.close();
        //分配内存，写入原始对象，生成新对象
        ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
        return new ObjectInputStream(ios);
    }

    public static <T> List<T> disperse(Configuration configuration, Class<T> clazz) {
        Map<String, String> props = CommonUtils.copy(configuration.getConfiguration());
        List<String> componentNames = CommonUtils.splitToList(configuration.getName(), "\\s+");
        List<T> ts = new ArrayList<>();
        for (String name : componentNames) {
            Configuration instance = (Configuration) newInstance(clazz);
            instance.setName(name);
            Map<String, String> instanceProps = filterByPrefix(props, Lists.newArrayList(name));
            instance.setConfiguration(kickOutKeyPrefix(instanceProps, name));
            instance.setName(name);
            ts.add((T) instance);
        }
        return ts;
    }

    public static Map<String, String> kickOutKeyPrefix(Map<String, String> properties, String prefix) {
        Map<String, String> props = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            props.put(key.replace(prefix + ".", ""), entry.getValue());
        }
        return props;
    }

    public static Map<String, String> attachKeyPrefix(Map<String, String> properties, String prefix) {
        Map<String, String> props = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            props.put(prefix + key, entry.getValue());
        }
        return props;
    }

    public static boolean containsKey(Map<String, String> props, String key) {
        Iterator<String> iterator = props.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if(StringUtils.startsWith(next, key)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String join(List<String> names, String split) {
        Joiner JOINER = Joiner.on(split);
        String[] strs = names.toArray(new String[names.size()]);
        return JOINER.join(strs);
    }

    public static String join(List<String> names) {
        return join(names, ".");
    }

    public static String join(String ... parts) {
        Joiner JOINER = Joiner.on(".");
        return JOINER.join(parts);
    }

    public static boolean containsAny(String str, String ... character) {
        if(str == null || str.length() == 0 || character == null || character.length == 0) {
            return false;
        }
        for (String ch : character) {
            if(str.indexOf(ch) > -1) {
                return true;
            }
        }
        return false;
    }

    public static String getIp() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                String name = networkInterface.getDisplayName();
                if(containsAny(name, "Loop", "WAN", "Virtual")) {
                    continue;
                }
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ip;
    }

    public static String duplicate(final String str, final String ch) {
        String split = ch + ch;
        String content = str;
        content = content.replace(split, ch);
        if(StringUtils.equals(content, str)) {
            return str;
        }
        return duplicate(content, ch);
    }

    public static String join(Collection<String> collection, String split) {
        if(CollectionUtils.isEmpty(collection)) {
            return "";
        }
        Iterator<String> iterator = collection.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String ele = iterator.next();
            if(StringUtils.isBlank(ele)) {
                continue;
            }
            sb.append(ele).append(split);
        }
        return sb.substring(0, sb.length() - split.length());
    }

    public static class MapKeyComparator<T> implements Comparator<T> {

        @Override
        public int compare(T o1, T o2) {
            Assert.isTrue(Comparable.class.isAssignableFrom(o1.getClass()), "比较类未实现Comparable接口");
            return ((Comparable) o1).compareTo(o2);
        }
    }
}


