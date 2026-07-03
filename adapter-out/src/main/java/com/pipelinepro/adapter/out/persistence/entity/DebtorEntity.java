package com.pipelinepro.adapter.out.persistence.entity;

import com.pipelinepro.domain.DebtorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
        name = "debtor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_debtor_national_number", columnNames = "national_number"),
                @UniqueConstraint(name = "uk_debtor_enterprise_number", columnNames = "enterprise_number")
        },
        indexes = {
                @Index(name = "idx_debtor_type_active", columnList = "type,active"),
                @Index(name = "idx_debtor_created_at", columnList = "created_at")
        })
public class DebtorEntity implements Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DebtorType type;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "national_number", length = 64)
    private String nationalNumber;

    @Column(name = "enterprise_number", length = 32)
    private String enterpriseNumber;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Transient
    private boolean newEntity = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    public void markNotNew() {
        this.newEntity = false;
    }

    @PostLoad
    @PostPersist
    void markNotNewAfterLoad() {
        this.newEntity = false;
    }

    public DebtorType getType() {
        return type;
    }

    public void setType(DebtorType type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    public void setNationalNumber(String nationalNumber) {
        this.nationalNumber = nationalNumber;
    }

    public String getEnterpriseNumber() {
        return enterpriseNumber;
    }

    public void setEnterpriseNumber(String enterpriseNumber) {
        this.enterpriseNumber = enterpriseNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
