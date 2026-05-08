package com.java.seed.order;

import com.java.seed.IntegrationTest;
import com.java.seed.api.model.OrderRequest;
import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
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
class OrderControllerIT extends IntegrationTest {

    private static final String ORDER_TOPIC = "order_topic";

    @Autowired
    private RestTestClient restTestClient;

    private KafkaConsumer<OrderEventKey, OrderEvent> kafkaConsumer = kafkaConsumer();

    private final Map<UUID, OrderEvent> mapOfEvents = new ConcurrentHashMap<>();

    @BeforeEach
    public void setup() {
        super.setup();
        kafkaConsumer.subscribe(Pattern.compile(ORDER_TOPIC));
    }

    @Test
    void shouldCreateOrder() {
        // given
        UUID userId = UUID.randomUUID();
        OrderRequest request = new OrderRequest().userId(userId);

        // when
        restTestClient.post()
                .uri("/v1/orders")
                .body(request)
                .exchange()
                .expectStatus()
                .isAccepted();

        // then
        kafkaConsumer.poll(Duration.ofSeconds(5))
                .forEach(this::processEvent);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(mapOfEvents.isEmpty()).isFalse();
                    OrderEvent event = mapOfEvents.get(userId);
                    assertThat(event.getUserId()).isEqualTo(userId);
                    assertThat(event.getOrderId()).isNotNull();
                    assertThat(event.getCreatedAt()).isNotNull();
                });
    }

    private void processEvent(ConsumerRecord<OrderEventKey, OrderEvent> record) {
        OrderEvent event = record.value();
        mapOfEvents.put(event.getUserId(), event);
    }
}
