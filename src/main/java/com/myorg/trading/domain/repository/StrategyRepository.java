package com.myorg.trading.domain.repository;

import com.myorg.trading.domain.entity.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    List<Strategy> findByUserId(Long userId);
    List<Strategy> findByUserIdAndActive(Long userId, Boolean active);
}