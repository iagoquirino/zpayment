package com.java.seed.user_order;

import com.java.seed.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static com.java.seed.shared.TestUtils.UserOrderInformation.givenUserOrder;

@AutoConfigureRestTestClient
class UserOrderControllerIT extends IntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserOrderRepository userOrderRepository;

    @Test
    void shouldListUserOrders() {
        // given
        UserOrder userOrder = givenUserOrder();
        userOrderRepository.save(userOrder);

        // when & then
        restTestClient.get()
                .uri("/v1/user-orders")
                .exchange()
                .expectStatus()
                .isOk().expectBody()
                .jsonPath("$[0].id").isEqualTo(userOrder.id().toString())
                .jsonPath("$[0].userId").isEqualTo(userOrder.userId().toString())
                .jsonPath("$[0].orderId").isEqualTo(userOrder.orderId().toString());
    }

}
