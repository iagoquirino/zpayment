package com.java.seed.user;

import com.java.seed.api.model.UserRequest;
import com.java.seed.shared.KafkaProperties;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final KafkaProperties kafkaProperties;

    private final KafkaTemplate<UserEventKey, UserEvent> kafkaTemplate;

    public UUID publish(UserRequest request) throws Exception {
        UUID userId = UUID.randomUUID();
        UserEventKey key = new UserEventKey(userId);
        UserEvent event = UserEvent.newBuilder()
                .setUserId(userId)
                .setName(request.getName())
                .setCreatedAt(Instant.now())
                .build();

        log.info("Publishing userId={}", event.getUserId());
        kafkaTemplate.send(getTopic(), key, event).get();
        log.info("UserEvent published successfully userId={}", event.getUserId());
        return userId;
    }

    private String getTopic() {
        return kafkaProperties
                .getTopics()
                .get("user");
    }
}
