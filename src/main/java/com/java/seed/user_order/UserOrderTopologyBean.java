package com.java.seed.user_order;

import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import com.java.seed.user_order.events.UserOrderEvent;
import com.java.seed.user_order.events.UserOrderEventKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.function.BiFunction;

@Slf4j
@Configuration
public class UserOrderTopologyBean {

    @Bean
    public BiFunction<KTable<UserEventKey, UserEvent>, KStream<OrderEventKey, OrderEvent>, KStream<UserOrderEventKey, UserOrderEvent>> userOrderTopology() {
        return (userTable, orderStream) -> orderStream
                .peek((key, value) -> log.info("Processing order event: {}", value))
                .selectKey((key, value) -> new UserEventKey(value.getUserId()))
                .join(userTable, (order, user) -> join(user, order))
                .selectKey((key, value) -> new UserOrderEventKey(value.getUserId(), value.getOrderId()));
    }

    private static UserOrderEvent join(UserEvent user, OrderEvent order) {
        log.info("Joining order {} with user {}", order.getOrderId(), user.getName());
        return UserOrderEvent.newBuilder()
                .setUserId(user.getUserId())
                .setName(user.getName())
                .setOrderId(order.getOrderId())
                .setCreatedAt(Instant.now())
                .build();
    }
}
