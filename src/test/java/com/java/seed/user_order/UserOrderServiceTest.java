package com.java.seed.user_order;

import com.java.seed.user_order.events.UserOrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.java.seed.shared.TestUtils.UserOrderInformation.givenUserOrder;
import static com.java.seed.shared.TestUtils.UserOrderInformation.givenUserOrderEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceTest {

    @Mock
    private UserOrderRepository repository;

    @Captor
    private ArgumentCaptor<UserOrder> captor;

    private UserOrderService testObj;

    @BeforeEach
    public void setup() {
        testObj = new UserOrderService(repository);
    }

    @Test
    void list() {
        // given
        List<UserOrder> entities = List.of(givenUserOrder());
        given(repository.findAll()).willReturn(entities);

        // when
        List<UserOrder> listFromDB = testObj.list();

        // then
        assertThat(listFromDB).containsAll(entities);
        then(repository).should().findAll();
    }

    @Test
    void process() {
        // given
        UserOrderEvent event = givenUserOrderEvent();

        // when
        testObj.process(event);

        // then
        then(repository).should().save(captor.capture());
        UserOrder capturedValue = captor.getValue();
        assertThat(capturedValue)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(event);

        assertThat(capturedValue.id()).isNotNull();
    }


}
