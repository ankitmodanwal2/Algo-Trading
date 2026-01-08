package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "strategies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Strategy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "template_id")
    private String templateId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "params_json", columnDefinition = "json")
    private String paramsJson;

    @Column(name = "active")
    private Boolean active = false;

    @Column(name = "total_trades")
    private Integer totalTrades = 0;

    @Column(name = "winning_trades")
    private Integer winningTrades = 0;

    @Column(name = "total_pnl", precision = 20, scale = 2)
    private BigDecimal totalPnl = BigDecimal.ZERO;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Double getWinRate() {
        if (totalTrades == 0) return 0.0;
        return (winningTrades.doubleValue() / totalTrades.doubleValue()) * 100.0;
    }
}