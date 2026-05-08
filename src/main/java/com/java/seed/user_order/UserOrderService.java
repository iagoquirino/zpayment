package com.java.seed.user_order;

import com.java.seed.user_order.events.UserOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class UserOrderService {

    private final UserOrderRepository userOrderRepository;

    List<UserOrder> list() {
        return userOrderRepository.findAll();
    }

    public void process(UserOrderEvent event) {
        UserOrder entity = build(event);
        userOrderRepository.save(entity);
        log.info("User order event persisted userId={}, orderId={}", event.getUserId(), event.getOrderId());
    }

    private UserOrder build(UserOrderEvent event) {
        return UserOrder.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .id(UUID.randomUUID())
                .build();
    }
}
