package com.java.seed.user_order;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserOrderRepository extends CrudRepository<UserOrder, UUID> {

    List<UserOrder> findAll();

}
