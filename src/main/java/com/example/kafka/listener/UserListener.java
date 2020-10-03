package com.example.kafka.listener;

import com.example.kafka.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
