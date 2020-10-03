package com.example.kafka.controller;

import com.example.kafka.entity.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
}
