package com.myorg.trading.domain.repository;

import com.myorg.trading.domain.entity.BrokerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, Long> {
    List<BrokerAccount> findByUserId(Long userId);
    Optional<BrokerAccount> findByUserIdAndBrokerId(Long userId, String brokerId);
}
