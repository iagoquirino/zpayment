package com.java.seed.shared;

import com.java.seed.order.events.OrderEvent;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user_order.UserOrder;
import com.java.seed.user_order.events.UserOrderEvent;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class TestUtils {

    public static  OrderEvent givenOrderEvent(UUID orderId, UUID userId) {
        return OrderEvent.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setCreatedAt(Instant.now())
                .build();
    }

    public static  UserEvent givenUserEvent(UUID userId) {
        return UserEvent.newBuilder()
                .setUserId(userId)
                .setName("John Doe")
                .setCreatedAt(Instant.now())
                .build();
    }

    public class UserOrderInformation {
        public static UserOrder givenUserOrder() {
            return UserOrder.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .orderId(UUID.randomUUID())
                    .build();
        }

        public static UserOrderEvent givenUserOrderEvent() {
            return UserOrderEvent.newBuilder()
                    .setUserId(UUID.randomUUID())
                    .setOrderId(UUID.randomUUID())
                    .setName("John Doe")
                    .setCreatedAt(Instant.now())
                    .build();
        }
    }


}
