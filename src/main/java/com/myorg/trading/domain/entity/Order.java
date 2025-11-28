package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "broker_account_id", nullable = false)
    private Long brokerAccountId;

    @Column(nullable = false, length = 128)
    private String symbol;

    @Column(length = 10)
    private String side; // BUY/SELL — keep as string to match canonical enum in service layer

    @Column(precision = 20, scale = 6)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 6)
    private BigDecimal price;

    @Column(name = "order_type", length = 20)
    private String orderType; // MARKET / LIMIT

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OrderStatus status;

    @Column(name = "broker_order_id", length = 128)
    private String brokerOrderId;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant executedAt;
}
