package com.myorg.trading.domain.repository;

import com.myorg.trading.domain.entity.ScheduledOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledOrderRepository extends JpaRepository<ScheduledOrder, Long> {
    List<ScheduledOrder> findByActiveTrue();
    List<ScheduledOrder> findByOrderId(Long orderId);
}
