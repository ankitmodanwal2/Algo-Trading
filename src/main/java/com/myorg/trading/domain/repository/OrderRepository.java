package com.myorg.trading.domain.repository;

import com.myorg.trading.domain.entity.Order;
import com.myorg.trading.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByBrokerAccountId(Long brokerAccountId);
    List<Order> findByStatus(OrderStatus status);
}
