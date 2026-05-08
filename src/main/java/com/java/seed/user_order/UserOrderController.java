package com.java.seed.user_order;

import com.java.seed.api.UserOrdersApi;
import com.java.seed.api.model.UserOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserOrderController implements UserOrdersApi {

    private final UserOrderService service;

    @Override
    public ResponseEntity<List<UserOrderResponse>> listUserOrders() {
        log.info("Retrieveing list of user order");
        List<UserOrderResponse> response = service.list()
                .stream()
                .map(this::map)
                .toList();
        log.info("Retrieveing list of user order");
        return ResponseEntity.ok(response);
    }

    private UserOrderResponse map(UserOrder entity) {
        return new UserOrderResponse()
                .id(entity.id())
                .userId(entity.userId())
                .orderId(entity.orderId());
    }
}
