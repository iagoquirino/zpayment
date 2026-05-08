package com.java.seed.order;

import com.java.seed.api.model.OrderRequest;
import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
import com.java.seed.shared.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaProperties kafkaProperties;

    private final KafkaTemplate<OrderEventKey, OrderEvent> kafkaTemplate;

    public UUID publish(OrderRequest request) throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderEventKey key = new OrderEventKey(orderId);
        OrderEvent event = OrderEvent.newBuilder()
                .setOrderId(orderId)
                .setUserId(request.getUserId())
                .setCreatedAt(Instant.now())
                .build();

        kafkaTemplate.send(getTopic(), key, event).get();
        log.info("Published order event orderId={}", event.getOrderId());
        return orderId;
    }

    private String getTopic() {
        return kafkaProperties.getTopics().get("order");
    }
}
