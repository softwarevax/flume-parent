# flume
flume 二次开发，对EmbeddedAgent的简易改造，动态控制agent，实现启动、关闭等功能。

## 1、用途

### 1.1、本地调试

```aidl
对flume不是特别熟悉的开发者，都没有办法一次开发完Source或Sink，改造完后方便本地在调试
```
### 1.2、开发ETL工具

```aidl
可开发ETL工具的好处是有具备事务的Channel，不会造成数据丢失，但如果要实现多种类型，有较大的开发量，可实现ETL功能
```
## 2、模块介绍

### 2.1、flume-engine

flume-engine是不可运行的jar包，要是其可以独立运行，添加启动类即可，或被依赖于其他可运行包中

#### 2.1.1、代码说明

com.softwarevax.flume.agent包下

##### 1、embedded

```aidl
    改造的EmbeddedAgent，可支持多Souece，Sink。原先的EmbeddedAgent，Source、Sink都只支持一个。若需功能如同命令行启动搬强大，需要对
EmbeddedAgentConfiguration.configure(String name, Map<String, String> props);
方法进行改造，使传入的Map<String, String>属性，解析成类似配置文件的格式返回。
    Source、Interceptor、Channel、Processor、Sink都是通过该方法的返回属性创建而来，详见：
MaterializedConfiguration conf = this.configurationProvider.get(this.name, properties);
```
##### 2、entity

```aidl
    通过传入实体的方式，解析成EmbeddedAgentConfiguration.configure(String name, Map<String, String> props)
方法的入参形式，使其能正常解析，该包都是些Source、Interceptor、Channel、Processor、Sink的载体。
```
#### 2.1.2、操作agent

```aidl
    创建一个AgentManager实体，可以提交、关闭agent
```
### 2.2、flume-client

```aidl
    web应用，用来提供agent启动、关闭的接口。可考虑新增一个类似网关的模块，agent都提交通过到网关模块，网关模块配置一些策略，
决定提交到哪个flume-client中运行，如负载均衡策略。
```
### 2.2、flume-api

```aidl
    含所有开发的Source、Interceptor、Sink，所有的拦截器均放在api-interceptor-flume模块，Souce和Sink都新建一个模块
```
## 3、自定义开发

> Source、Interceptor、Channel、Processor、Sink暂且都称为组件
### 3.1、Configurable

```aidl

实现了Configurable接口的组件，在调用EmbeddedAgent.configure(Map<String, String> configure)时就会回调接口中的唯一方法，
不需要等到调用EmbeddedAgent.start();
```
### 3.2、Source

```aidl
    Source分为PollableSource和EventDrivenSource，关系数据库，还有消息中间件（RocketMQ、Kafka），基本都是PollableSource类型，
RabbitMQ是EventDrivenSource类型的，具体实现哪种Source，取决于获取数据的方式。PollableSource类型的process()方法，如果返回
Status.BACKOFF，经过getBackOffSleepIncrement()时间后会再次调用，如果返回Status.READY，执行完之后，就会再次进入process()方法。
```
### 3.3、Interceptor

```aidl
    Interceptor是依附在Source上的，配置的拦截器，要指是哪个定Source的拦截器。实现接口Interceptor即可，若需要配置参数，再实现接口
Configurable，在拦截器上，可以做一个简单处理，比如碰到字符串为null，将他改为""。
```
### 3.4、Channel

```aidl
    transactionCapacity默认为100，如果一次提交超过100条数据，则会提交失败。capacity是Channel的容量，Channel有file、menory等类型，
详见ChannelType。
```
### 3.5、Sink

```aidl
Sink需要开启事务，防止数据丢失。
Transaction transaction = channel.getTransaction();
transaction.begin();
transaction.commit();
transaction.close();
transaction.rollback();
```
## 4、agent启动和关闭例子

将t_user的数据，复制到t_user_copy表中

### 4.1、启动flume-client

### 4.2、调用启动接口

```aidl
http://localhost:8080/start

Content-Type: application/json

{
    "channel": {
        "configuration": {
            "type": "MEMORY",
            "transactionCapacity": "1000",
            "capacity": "1000000"
        },
        "type": "MEMORY"
    },
    "name": "mysql",
    "processor": {
        "configuration": {
            "type": "DEFAULT"
        },
        "type": "DEFAULT"
    },
    "sink": {
        "sinks": [
            {
                "configuration": {
                    "type": "com.softwarevax.flume.sink.mysql.MySQLSink",
                    "driverClassName": "com.mysql.cj.jdbc.Driver",
                    "username": "root",
                    "password": "123456",
                    "url": "jdbc:mysql://localhost:3306/optimize?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
                    "table": "t_user_copy",
                    "batch.size": "1000",
                    "columns": "id,name,nick_name,id_card_no,sex,phone,email,wechat"
                },
                "name": "s1"
            }
        ]
    },
    "source": {
        "sources": [
            {
                "configuration": {
                    "driverClassName": "com.mysql.cj.jdbc.Driver",
                    "username": "root",
                    "password": "123456",
                    "url": "jdbc:mysql://localhost:3306/optimize?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
                    "type": "com.softwarevax.flume.source.mysql.MySQLSource",
                    "table": "t_user",
                    "fetch.size": "1000"
                },
                "interceptors": [
                    {
                        "configuration": {
                            "type": "com.softwarevax.flume.interceptor.TimeStampInterceptor$Builder"
                        },
                        "name": "interceptor_1"
                    }
                ],
                "name": "r1"
            }
        ]
    }
}
```
### 4.3、查看flume agent的启动的日志

HeadTagInterceptor是默认的拦截器，可以将名字设置为interceptor_0，覆盖默认的拦截器

```aidl
mysql.channels=mysql-channel
mysql.channels.mysql-channel.capacity=1000000
mysql.channels.mysql-channel.transactionCapacity=1000
mysql.channels.mysql-channel.type=MEMORY
mysql.sinkgroups=mysql-sink-group
mysql.sinkgroups.mysql-sink-group.processor.type=DEFAULT
mysql.sinkgroups.mysql-sink-group.sinks=s1
mysql.sinks=s1
mysql.sinks.s1.batch.size=1000
mysql.sinks.s1.channel=mysql-channel
mysql.sinks.s1.columns=id,name,nick_name,id_card_no,sex,phone,email,wechat
mysql.sinks.s1.driverClassName=com.mysql.cj.jdbc.Driver
mysql.sinks.s1.password=123456
mysql.sinks.s1.table=t_user_copy
mysql.sinks.s1.type=com.softwarevax.flume.sink.mysql.MySQLSink
mysql.sinks.s1.url=jdbc:mysql://localhost:3306/optimize?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
mysql.sinks.s1.username=root
mysql.sources=r1
mysql.sources.r1.channels=mysql-channel
mysql.sources.r1.driverClassName=com.mysql.cj.jdbc.Driver
mysql.sources.r1.fetch.size=1000
mysql.sources.r1.interceptors=r1_interceptor_0 r1_interceptor_1
mysql.sources.r1.interceptors.r1_interceptor_0.type=com.softwarevax.flume.interceptor.HeadTagInterceptor$Builder
mysql.sources.r1.interceptors.r1_interceptor_1.type=com.softwarevax.flume.interceptor.TimeStampInterceptor$Builder
mysql.sources.r1.password=123456
mysql.sources.r1.table=t_user
mysql.sources.r1.type=com.softwarevax.flume.source.mysql.MySQLSource
mysql.sources.r1.url=jdbc:mysql://localhost:3306/optimize?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
mysql.sources.r1.username=root
```
### 4.4、查看flume agent的关闭打印的日志

```aidl
Component type: CHANNEL, name: mysql-channel stopped
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.start.time == 1671597763413
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.stop.time == 1671597786235
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.capacity == 1000000
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.current.size == 18000
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.event.put.attempt == 137000
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.event.put.success == 137000
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.event.take.attempt == 119000
Shutdown Metric for type: CHANNEL, name: mysql-channel. channel.event.take.success == 119000
Source runner interrupted. Exiting
```
### 4.5、属性方式启动

```aidl
Map<String, String> properties = new HashMap<>();
// source
properties.put("sources", "r1 r2");

properties.put("r1.type", "com.softwarevax.flume.source.MySource");
properties.put("r1.pre", " r1-local ");
properties.put("r1.sub", " r1-host ");
properties.put("r1.delay", "1000");
// 设置拦截器[0代表顺序, 可覆盖公用的拦截器]
properties.put("r1.interceptors[1].type", "com.softwarevax.flume.interceptor.TimeStampInterceptor$Builder");
// 设置拦截器属性
properties.put("r1.interceptors[1].name", "张三");

properties.put("r2.type", "com.softwarevax.flume.source.MySource2");
properties.put("r2.pre", " r2-local ");
properties.put("r2.sub", " r2-host ");
properties.put("r2.delay", "1000");

// memory、file(为每个任务设置相应的路径)
properties.put("channel.type", "file");
properties.put("channel.capacity", "100000");

// sink group
properties.put("sinks", "s1 s2");

// s1
properties.put("s1.type", "com.softwarevax.flume.sink.MySink");
properties.put("s1.pre", "s1-");
properties.put("s1.sub", "-s1");

// s2
properties.put("s2.type", "com.softwarevax.flume.sink.MySink2");
properties.put("s2.pre", "s2-");
properties.put("s2.sub", "-s2");

// processor负载均衡
properties.put("processor.type", "load_balance");
// type = load_balance时，可自定义selector，默认ROUND_ROBIN
properties.put("processor.selector", "round_robin");

try {
    EmbeddedAgent agent = new EmbeddedAgent("agent");
    agent.configure(properties);
    agent.start();
} catch (final Exception ex) {
}
```
### 4.6、实体方式启动

```aidl
AgentEntity entity = new AgentEntity("mysql");

AgentSource agentSource = new AgentSource();
List<Source> sources = new ArrayList<>();
// s1
Source source1 = new Source();
source1.setName("r1");
Map<String, String> r1Map = new HashMap<>();
r1Map.put("type", "com.softwarevax.flume.source.MySource");
r1Map.put("pre", " r1-local ");
r1Map.put("sub", " r1-host ");
r1Map.put("delay", "1000");
AgentInterceptor interceptor = new AgentInterceptor();
interceptor.setName("interceptor_1");
Map<String, String> interceptorMap = new HashMap<>();
interceptorMap.put("type", "com.softwarevax.flume.interceptor.TimeStampInterceptor$Builder");
interceptorMap.put("tag", "vax");
interceptor.setConfiguration(interceptorMap);
source1.setConfiguration(r1Map);
List<AgentInterceptor> interceptors = new ArrayList<>();
interceptors.add(interceptor);
source1.setInterceptors(interceptors);
sources.add(source1);

Source source2 = new Source();
source2.setName("r2");
Map<String, String> r2Map = new HashMap<>();
r2Map.put("type", "com.softwarevax.flume.source.MySource2");
r2Map.put("pre", " r2-local ");
r2Map.put("sub", " r2-host ");
r2Map.put("delay", "1000");
source2.setConfiguration(r2Map);
sources.add(source2);

agentSource.setSources(sources);
entity.setSource(agentSource);

AgentChannel channel = new AgentChannel();
Map<String, String> channelMap = new HashMap<>();
channelMap.put("type", "file");
channelMap.put("capacity", "100000");
channel.setConfiguration(channelMap);
entity.setChannel(channel);

AgentProcessor processor = new AgentProcessor();
Map<String, String> processorMap = new HashMap<>();
// 多个sink时，type不能为default，一个sink时，type不能为load_balance
processorMap.put("type", "load_balance");
processorMap.put("selector", "round_robin");
processor.setConfiguration(processorMap);
entity.setProcessor(processor);

AgentSink agentSink = new AgentSink();
List<Sink> sinks = new ArrayList<>();
// sink1
Sink sink1 = new Sink();
Map<String, String> s1Map = new HashMap<>();
sink1.setName("s1");
s1Map.put("type", "com.softwarevax.flume.sink.MySink");
s1Map.put("pre", "s1-");
s1Map.put("sub", "-s1");
sink1.setConfiguration(s1Map);
sinks.add(sink1);
// sink2
Sink sink2 = new Sink();
Map<String, String> s2Map = new HashMap<>();
sink2.setName("s2");
s2Map.put("type", "com.softwarevax.flume.sink.MySink2");
s2Map.put("pre", "s2-");
s2Map.put("sub", "-s2");
sink2.setConfiguration(s2Map);
sinks.add(sink2);

agentSink.setSinks(sinks);
entity.setSink(agentSink);

AgentManager manager = new AgentManager();
Map<String, String> props = manager.configure(entity);
try {
    EmbeddedAgent agent = new EmbeddedAgent("agent");
    agent.configure(props);
    agent.start();
} catch (final Exception ex) {
}
```
