package com.java.seed.user_order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.java.seed.shared.TestUtils.UserOrderInformation.givenUserOrder;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserOrderController.class)
class UserOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserOrderService userOrderService;

    @Test
    void shouldListUserOrders() throws Exception {
        // given
        UserOrder userOrder = givenUserOrder();
        given(userOrderService.list()).willReturn(List.of(userOrder));

        // when & then
        mockMvc.perform(get("/v1/user-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userOrder.id().toString()))
                .andExpect(jsonPath("$[0].userId").value(userOrder.userId().toString()))
                .andExpect(jsonPath("$[0].orderId").value(userOrder.orderId().toString()));
    }


}
