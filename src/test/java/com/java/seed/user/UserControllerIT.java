package com.java.seed.user;

import com.java.seed.IntegrationTest;
import com.java.seed.api.model.UserRequest;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureRestTestClient
class UserControllerIT extends IntegrationTest {

    private static final String USER_TOPIC = "user_topic";

    @Autowired
    private RestTestClient restTestClient;

    private KafkaConsumer<UserEventKey, UserEvent> kafkaConsumer = kafkaConsumer();

    private final Map<UUID, UserEvent> mapOfEvents = new ConcurrentHashMap<>();

    @BeforeEach
    public void setup() {
        super.setup();
        kafkaConsumer.subscribe(Pattern.compile(USER_TOPIC));
    }

    @Test
    void shouldCreateUser() {
        // given
        UserRequest request = new UserRequest().name("John Doe");
        // when
        restTestClient.post()
                .uri("/v1/users")
                .body(request)
                .exchange()
                .expectStatus()
                .isAccepted();

        // then
        kafkaConsumer.poll(Duration.ofSeconds(5))
                .forEach(this::processEvent);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(mapOfEvents.isEmpty()).isFalse());
    }

    private void processEvent(ConsumerRecord<UserEventKey, UserEvent> record) {
        UserEvent event = record.value();
        mapOfEvents.put(event.getUserId(), event);
    }
}
