package com.pipelinepro.adapter.out.persistence.entity;

import com.pipelinepro.domain.MatchConfidence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
        name = "allocation_proposal_candidate",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_proposal_candidate_rank", columnNames = {"proposal_id", "rank_order"}),
                @UniqueConstraint(name = "uk_proposal_candidate_debt", columnNames = {"proposal_id", "debt_id"})
        },
        indexes = {
                @Index(name = "idx_proposal_candidate_debtor", columnList = "debtor_id"),
                @Index(name = "idx_proposal_candidate_debt", columnList = "debt_id")
        })
public class AllocationProposalCandidateEntity implements Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proposal_id", nullable = false)
    private UUID proposalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_candidate_proposal"))
    private AllocationProposalEntity proposal;

    @Column(name = "debtor_id", nullable = false)
    private UUID debtorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debtor_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_candidate_debtor"))
    private DebtorEntity debtor;

    @Column(name = "debt_id", nullable = false)
    private UUID debtId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debt_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_candidate_debt"))
    private DebtEntity debt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MatchConfidence confidence;

    @Column(name = "suggested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal suggestedAmount;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

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

    public UUID getProposalId() {
        return proposalId;
    }

    public void setProposalId(UUID proposalId) {
        this.proposalId = proposalId;
    }

    public AllocationProposalEntity getProposal() {
        return proposal;
    }

    public void setProposal(AllocationProposalEntity proposal) {
        this.proposal = proposal;
    }

    public UUID getDebtorId() {
        return debtorId;
    }

    public void setDebtorId(UUID debtorId) {
        this.debtorId = debtorId;
    }

    public DebtorEntity getDebtor() {
        return debtor;
    }

    public void setDebtor(DebtorEntity debtor) {
        this.debtor = debtor;
    }

    public UUID getDebtId() {
        return debtId;
    }

    public void setDebtId(UUID debtId) {
        this.debtId = debtId;
    }

    public DebtEntity getDebt() {
        return debt;
    }

    public void setDebt(DebtEntity debt) {
        this.debt = debt;
    }

    public MatchConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(MatchConfidence confidence) {
        this.confidence = confidence;
    }

    public BigDecimal getSuggestedAmount() {
        return suggestedAmount;
    }

    public void setSuggestedAmount(BigDecimal suggestedAmount) {
        this.suggestedAmount = suggestedAmount;
    }

    public Integer getRankOrder() {
        return rankOrder;
    }

    public void setRankOrder(Integer rankOrder) {
        this.rankOrder = rankOrder;
    }
}
