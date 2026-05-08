package com.java.seed.user_order;

import com.java.seed.user_order.events.UserOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserOrderListenerBean {

    private final UserOrderService userOrderService;

    @Bean
    public Consumer<UserOrderEvent> userOrderListener() {
        return event -> {
            log.info("Received user order event: userId={}, orderId={}", event.getUserId(), event.getOrderId());
            userOrderService.process(event);
        };
    }
}
