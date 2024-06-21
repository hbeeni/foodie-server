package com.been.foodieserver.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${spring.kafka.topic.slack}")
    private String slackTopic;

    @Value("${spring.kafka.topic.post}")
    private String postTopic;

    @Bean
    public NewTopic notificationTopic() {
        return new NewTopic(notificationTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic slackTopic() {
        return new NewTopic(slackTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic postTopic() {
        return new NewTopic(postTopic, 1, (short) 1);
    }
}
