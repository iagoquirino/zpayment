package com.java.seed.user_order;

import com.java.seed.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserOrderRepositoryIT extends IntegrationTest {

    @Autowired
    private UserOrderRepository repository;

    @Test
    void findAll() {
        // given
        UserOrder order = new UserOrder(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        repository.save(order);

        // when
        List<UserOrder> result = repository.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(order.id());
    }

    @Test
    void save() {
        UUID id = UUID.randomUUID();
        UserOrder order = new UserOrder(id, UUID.randomUUID(), UUID.randomUUID());
        
        repository.save(order);
        
        assertThat(repository.existsById(id)).isTrue();
    }
}
