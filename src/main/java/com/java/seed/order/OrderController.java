package com.java.seed.order;

import com.java.seed.api.OrdersApi;
import com.java.seed.api.model.OrderRequest;
import com.java.seed.api.model.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) throws Exception {
        UUID orderId = orderService.publish(orderRequest);
        return ResponseEntity.accepted().body(new OrderResponse()
                .orderId(orderId)
                .result(OrderResponse.ResultEnum.SUCCESS));
    }
}
