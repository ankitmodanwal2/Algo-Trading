package com.myorg.trading.domain.repository;

import com.myorg.trading.domain.entity.ExitStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExitStrategyRepository extends JpaRepository<ExitStrategy, Long> {
    List<ExitStrategy> findByOrderIdAndActive(Long orderId, boolean active);
    List<ExitStrategy> findByActive(boolean active);
}
