package com.example.kafka.configuration;

import com.example.kafka.entity.User;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.*;

import java.util.HashMap;
import java.util.Map;

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
