# 一、简介

Kafka 是一个分布式消息队列

# 二、说明

## 设计

- 生产者
    - 负载均衡
    - 批量异步发送
    
- 消费者
    - 批量拉取数据
    - 监听轮询、控制传输大小
    - 单个分区单个消费者维持偏移量
    - 流处理提供持久消费者ID
    
- 消息传递
    - 幂等发送
    - 流处理提供恰好一次传递

## 高可用

- 主从复制
    - 通过zookeeper心跳机制保持会话
    - 从节点同步主节点写入
    - 平衡分区主从

- ISR机制
    - 只需两个副本和一次确认
    - 无需为崩溃副本恢复完整数据
    - 全部崩溃后则等待ISR副本或选择第一个副本

- ack机制


## 高并发

- 持久化
    - 预读和后写
    - 顺序磁盘访问可能比内存随机访问快
    - 数据立即写入文件系统的持久日志，不用刷新到磁盘
    - 持久化队列将读取附加到文件
    - 组合消息批量处理
    - 使用sendfile，允许OS将数据从页面缓存直接发送到网络来避免重新复制
    - 端到端批量压缩

## 日志

- 压缩
    - 流处理保证最终结果



## 数据写入过程

- 1、生产者先从 zookeeper 的 "/brokers/…/state"节点找到该 partition 的主节点
- 2、生产者将消息发送给主节点
- 3、主节点将消息写入本地 log
- 4、从节点从主节点pull消息，写入本地log后向主节点发送 ACK
- 5、主节点收到所有ISR中的候选节点的ACK后，增加HW并向producer发送ACK

## 数据读取过程


## 流式处理

用于构建关键任务实时应用程序和微服务的客户端库，其中输入和/或输出数据存储在Kafka集群中

# 三、使用

## Docker

### 1、新建docker-compose.yml

```yaml
version: '3'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    depends_on: [ zookeeper ]
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 0
      KAFKA_ZOOKEEPER_CONNECT: 172.28.121.21:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://172.28.121.21:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
    volumes:
      - /data/kafka/docker.sock:/var/run/docker.sock
```

### 2、docker-compose部署

```shell script
docker-compose up -d
```

## 命令

### 服务





### Topic




### 生产者


### 消费者



## JAVA

### 1、新建一个Spring Initializr项目

### 2、添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
</dependency>

<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.2.5</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 3、修改配置文件

```properties
server.servlet.context-path=/kafka
server.port=8888
spring.kafka.streams.application-id=kafka
spring.kafka.streams.properties.cache.max.bytes.buffering=0

spring.kafka.bootstrap-servers=10.192.77.202:9092
#spring.main.allow-bean-definition-overriding=true
spring.kafka.consumer.properties.isolation.level=read_committed
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.IntegerDeserializer

#spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.IntegerSerializer
#spring.kafka.producer.transaction-id-prefix=tx-
spring.kafka.producer.properties.spring.json.type.mapping=user:com.example.kafka.entity.User

spring.kafka.listener.type=batch

spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://10.192.77.202:5438/test
spring.datasource.username=postgres
spring.datasource.password=wW083MPnzwvX
```

### 4、创建实体类

```java
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private Integer age;
}
```

### 5、构建kafka基础配置类

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public RecordMessageConverter converter() {
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("com.example.kafka.entity");
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("user", User.class);
        typeMapper.setIdClassMapping(mappings);
        converter.setTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public BatchMessagingMessageConverter batchConverter() {
        return new BatchMessagingMessageConverter(converter());
    }

    @Bean
    public NewTopic topic1() {
        return TopicBuilder.name("user").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topic2() {
        return TopicBuilder.name("user1").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topic3() {
        return TopicBuilder.name("user2").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topic4() {
        return TopicBuilder.name("test").partitions(1).replicas(1).build();
    }
}
```

### 6、构建kafka事务配置类

```java
@Configuration
@EnableKafka
public class KafkaTransactionConfig {
    @Bean
    public ChainedKafkaTransactionManager<Object, Object> chainedTm(
            KafkaTransactionManager<String, String> ktm,
            DataSourceTransactionManager dstm) {
        return new ChainedKafkaTransactionManager<>(ktm, dstm);
    }

    @Bean
    public DataSourceTransactionManager dstm(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            ChainedKafkaTransactionManager<Object, Object> chainedTM) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.getContainerProperties().setTransactionManager(chainedTM);
        return factory;
    }
}
```

### 7、构建kafka流处理配置类

```java
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {
    public static final String INPUT_TOPIC = "streams-plaintext-input";
    public static final String OUTPUT_TOPIC = "streams-wordcount-output";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer customizer() {
        return fb -> fb.setStateListener((newState, oldState) -> {
            System.out.println("State transition from " + oldState + " to " + newState);
        });
    }

    @Bean
    public KStream<String, Long> kStream1(StreamsBuilder kStreamBuilder) {
        KStream<String, String> source = kStreamBuilder.stream(INPUT_TOPIC);

        KStream<String, Long> stream = source
                .flatMapValues(value -> {
                    return Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" "));
                })
                .groupBy((key, value) -> value)
                .count()
                .toStream();
        stream.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

        return stream;
    }

    @Bean
    public KStream<Integer, String> kStream2(StreamsBuilder kStreamBuilder) {
        KStream<Integer, String> stream = kStreamBuilder.stream("streamingTopic1");
        stream
                .mapValues((ValueMapper<String, String>) String::toUpperCase)
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMillis(1000)))
                .reduce((String value1, String value2) -> value1 + value2,
                        Named.as("windowStore"))
                .toStream()
                .map((windowedId, value) -> new KeyValue<>(windowedId.key(), value))
                .filter((i, s) -> s.length() > 40)
                .to("streamingTopic2");

        stream.print(Printed.toSysOut());

        return stream;
    }
}
```

### 8、创建Listener

```java
@Component
public class UserListener {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(id = "userGroup1", topics = "user1")
    public void listen1(String in) {
        this.kafkaTemplate.send("user2", in.toUpperCase());
        this.jdbcTemplate.execute("insert into mytable (data) values ('" + in + "')");
    }

    @KafkaListener(id = "userGroup2", topics = "user2")
    public void listen2(String in) {
        System.out.println(in);
    }

    @KafkaListener(id = "userGroup3", topics = "user2")
    public void listenBatch(List<User> usres) {
        usres.forEach(f -> System.out.println("Received: " + f));
    }
}
```

### 9、定制Consumer

```java
@Component
@KafkaListener(id = "userGroup", topics = {"user"})
public class UserConsumer {
    @KafkaHandler
    public void user(User user) {
        System.out.println("Received: " + user);
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        System.out.println("Received unknown: " + object);
    }
}
```

### 10、创建Controller

```java
@RestController
public class Controller {
    @Resource
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @PostMapping(path = "/user")
    public void sendUser() {
        User user = new User(1L, "小仙女", 1);
        kafkaTemplate.send("user", user);
    }

    @PostMapping(path = "/stream")
    public void sendStream() {
        kafkaTemplate.send("streams-plaintext-input", "all streams lead to kafka");
    }

    @PostMapping(path = "/unknown")
    public void sendUnknown() {
        kafkaTemplate.send("user", "unknown");
    }

    @PostMapping(path = "/users")
    public void sendUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "小仙女1", 1));
        users.add(new User(2L, "小仙女2", 2));
        users.add(new User(3L, "小仙女3", 3));
        users.add(new User(4L, "小仙女4", 4));
        users.add(new User(5L, "小仙女5", 5));
        kafkaTemplate.executeInTransaction(kafkaTemplate -> {
            users.stream().forEach(user -> kafkaTemplate.send("user1", user));
            return null;
        });
    }
}git commit -m "first commit"
```

### 11、Test方式验证

```java
@RunWith(SpringRunner.class)
@SpringBootTest
class KafkaApplicationTests {
    @Resource
    private KafkaTemplate<Integer, String> template;

    @Test
    public void testTemplate() throws Exception {
        template.setDefaultTopic("test");
        template.sendDefault("小仙女");
        template.sendDefault(0, 2, "大怪兽");

        Map<String, Object> consumerProps = KafkaTestUtils
                .consumerProps("10.192.77.202:9092", "testGroup", "false");
        DefaultKafkaConsumerFactory<Integer, String> cf =
                new DefaultKafkaConsumerFactory<Integer, String>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("test");
        KafkaMessageListenerContainer<Integer, String> container =
                new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<Integer, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<Integer, String>) record -> {
            records.add(record);
        });
        container.setBeanName("templateTests");
        container.start();

        ConsumerRecord<Integer, String> received1 = records.poll(10, TimeUnit.SECONDS);
        System.out.println(received1);
        assertThat(received1, hasValue("小仙女"));
        ConsumerRecord<Integer, String> received2 = records.poll(10, TimeUnit.SECONDS);
        System.out.println(received2);
        assertThat(received2, hasKey(2));
        assertThat(received2, hasPartition(0));
        assertThat(received2, hasValue("大怪兽"));
    }
}
```

# 四、链接

[Kafka官网](http://kafka.apache.org/ "Kafka官网")

[spring-kafka官网](https://spring.io/projects/spring-kafka/ "spring-kafka官网")

[Docker-Kafka](https://hub.docker.com/r/wurstmeister/kafka "Docker-Kafka")