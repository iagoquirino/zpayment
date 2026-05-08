package com.java.seed.order;

import com.java.seed.api.model.OrderRequest;
import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
import com.java.seed.shared.KafkaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String ORDER_TOPIC = "order_topic";

    @Mock
    private KafkaTemplate<OrderEventKey, OrderEvent> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<OrderEventKey, OrderEvent>> future;

    @Mock
    private KafkaProperties kafkaProperties;

    @Captor
    private ArgumentCaptor<OrderEventKey> keyCaptor;

    @Captor
    private ArgumentCaptor<OrderEvent> eventCaptor;

    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderService = new OrderService(kafkaProperties, kafkaTemplate);
        given(kafkaProperties.getTopics()).willReturn(Map.of("order", ORDER_TOPIC));
    }

    @Test
    void shouldPublishOrderEvent() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        OrderRequest request = new OrderRequest().userId(userId);

        given(kafkaTemplate.send(anyString(), any(), any())).willReturn(future);

        // when
        UUID orderId = orderService.publish(request);

        // then
        assertThat(orderId).isNotNull();
        
        then(kafkaTemplate).should().send(eq(ORDER_TOPIC), keyCaptor.capture(), eventCaptor.capture());
        
        assertThat(keyCaptor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(eventCaptor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().getCreatedAt()).isNotNull();
    }
}
