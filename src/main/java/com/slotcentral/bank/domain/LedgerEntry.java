package com.slotcentral.bank.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_uid", nullable = false)
    private String accountUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    /** Amount in credits. Positive = credit (deposit, win, refund). Negative = debit (withdraw, buy-out, bet reserve). */
    @Column(nullable = false)
    private Long amount;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "egm_id")
    private String egmId;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "trans_by")
    private String transBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public String getAccountUid() { return accountUid; }
    public void setAccountUid(String accountUid) { this.accountUid = accountUid; }
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getEgmId() { return egmId; }
    public void setEgmId(String egmId) { this.egmId = egmId; }
    public Long getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Long balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getTransBy() { return transBy; }
    public void setTransBy(String transBy) { this.transBy = transBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
