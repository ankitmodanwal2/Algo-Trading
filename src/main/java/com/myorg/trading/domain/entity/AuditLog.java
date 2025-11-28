package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String action; // e.g. PLACE_ORDER, CANCEL_ORDER, LINK_BROKER

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson; // store request/response snapshot (encrypted if sensitive)

    @CreationTimestamp
    private Instant createdAt;
}
