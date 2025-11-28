package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "scheduled_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "trigger_time")
    private Instant triggerTime; // single-run trigger

    @Column(name = "cron_expression", length = 128)
    private String cronExpression; // for recurring schedules (nullable)

    @Column(name = "quartz_job_key", length = 255)
    private String quartzJobKey; // Quartz job identifier

    private boolean active = true;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
