package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "exit_strategies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitStrategy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "strategy_type", length = 64)
    private String strategyType; // TIME_BASED, TARGET_STOP, TRAILING_STOP, OCO

    @Column(name = "params_json", columnDefinition = "json")
    private String paramsJson; // JSON-encoded parameters for the strategy

    private boolean active = true;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
