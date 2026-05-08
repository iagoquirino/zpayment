package com.java.seed.user_order;

import com.java.seed.IntegrationTest;
import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import com.java.seed.user_order.events.UserOrderEvent;
import com.java.seed.user_order.events.UserOrderEventKey;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.java.seed.shared.TestUtils.givenOrderEvent;
import static com.java.seed.shared.TestUtils.givenUserEvent;
import static org.assertj.core.api.Assertions.assertThat;

class UserOrderTopologyBeanIT extends IntegrationTest {

    private static final String USER_TOPIC = "user_topic";
    private static final String ORDER_TOPIC = "order_topic";
    private static final String OUTPUT_TOPIC = "user_order_topic";

    private final Map<UUID, UserOrderEvent> mapOfEvents = new ConcurrentHashMap<>();

    @Autowired
    private KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

    private KafkaConsumer<UserOrderEventKey, UserOrderEvent> kafkaConsumer = kafkaConsumer();

    @BeforeEach
    public void setup() {
        super.setup();
        kafkaConsumer.subscribe(List.of(OUTPUT_TOPIC));
    }

    @Test
    void shouldJoinUserAndOrderEvents() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserEvent user = givenUserEvent(userId);

        UUID orderId = UUID.randomUUID();
        OrderEvent order = givenOrderEvent(orderId, userId);

        //when
        kafkaTemplate.send(new ProducerRecord<>(USER_TOPIC, new UserEventKey(userId), user)).get();
        kafkaTemplate.send(new ProducerRecord<>(ORDER_TOPIC, new OrderEventKey(orderId), order)).get();

        // then
        kafkaConsumer.poll(Duration.ofSeconds(5))
                        .forEach(this::processEvent);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(mapOfEvents.isEmpty()).isFalse();
                    UserOrderEvent event = mapOfEvents.get(orderId);
                    assertThat(event.getUserId()).isEqualTo(userId);
                    assertThat(event.getOrderId()).isEqualTo(orderId);
                    assertThat(event.getName().toString()).isEqualTo("John Doe");
                });
    }

    private void processEvent(ConsumerRecord<UserOrderEventKey, UserOrderEvent> record) {
        UserOrderEvent event = record.value();
        mapOfEvents.put(event.getOrderId(), event);
    }

}
