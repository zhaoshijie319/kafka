package com.example.kafka.consumer;

import com.example.kafka.entity.User;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
