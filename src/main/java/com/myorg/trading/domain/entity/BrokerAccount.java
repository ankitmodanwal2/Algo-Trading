package com.myorg.trading.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "broker_accounts", indexes = {
        @Index(name = "idx_broker_user", columnList = "user_id, broker_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // referenced user id (we avoid direct User relation for simpler token store handling)

    @Column(name = "broker_id", nullable = false, length = 50)
    private String brokerId; // e.g. "dhan", "fyers", "angelone"

    @Lob
    @Column(name = "credentials_encrypted", columnDefinition = "text")
    private String credentialsEncrypted; // encrypted JSON or token blob (must be encrypted)

    @Column(name = "metadata_json", columnDefinition = "json")
    private String metadataJson; // broker specific meta (nullable)

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
