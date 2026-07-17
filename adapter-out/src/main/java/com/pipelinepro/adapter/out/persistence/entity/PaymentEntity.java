package com.pipelinepro.adapter.out.persistence.entity;

import com.pipelinepro.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_bank_transaction_reference",
                        columnNames = "bank_transaction_reference")
        },
        indexes = {
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_created_at", columnList = "created_at")
        })
public class PaymentEntity implements Persistable<UUID> {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "bank_transaction_reference", nullable = false, length = 100)
    private String bankTransactionReference;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "remaining_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status;

    @Column(name = "structured_communication", length = 64)
    private String structuredCommunication;

    @Column(name = "free_communication", length = 500)
    private String freeCommunication;

    @Column(name = "payer_name", length = 200)
    private String payerName;

    @Column(name = "payer_iban_masked", length = 64)
    private String payerIbanMasked;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    @PrePersist
    void assignIdIfMissing() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public String getBankTransactionReference() {
        return bankTransactionReference;
    }

    public void setBankTransactionReference(String bankTransactionReference) {
        this.bankTransactionReference = bankTransactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getStructuredCommunication() {
        return structuredCommunication;
    }

    public void setStructuredCommunication(String structuredCommunication) {
        this.structuredCommunication = structuredCommunication;
    }

    public String getFreeCommunication() {
        return freeCommunication;
    }

    public void setFreeCommunication(String freeCommunication) {
        this.freeCommunication = freeCommunication;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerIbanMasked() {
        return payerIbanMasked;
    }

    public void setPayerIbanMasked(String payerIbanMasked) {
        this.payerIbanMasked = payerIbanMasked;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
