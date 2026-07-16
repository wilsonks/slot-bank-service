package com.slotcentral.bank.dto;

import com.slotcentral.bank.domain.EntryType;
import com.slotcentral.bank.domain.LedgerEntry;
import java.time.OffsetDateTime;

public class LedgerEntryResponse {
    private Long id;
    private String accountUid;
    private EntryType entryType;
    private Long amount;
    private String referenceId;
    private String egmId;
    private Long balanceAfter;
    private String transBy;
    private OffsetDateTime createdAt;

    public static LedgerEntryResponse from(LedgerEntry e) {
        LedgerEntryResponse r = new LedgerEntryResponse();
        r.id = e.getId();
        r.accountUid = e.getAccountUid();
        r.entryType = e.getEntryType();
        r.amount = e.getAmount();
        r.referenceId = e.getReferenceId();
        r.egmId = e.getEgmId();
        r.balanceAfter = e.getBalanceAfter();
        r.transBy = e.getTransBy();
        r.createdAt = e.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getAccountUid() { return accountUid; }
    public EntryType getEntryType() { return entryType; }
    public Long getAmount() { return amount; }
    public String getReferenceId() { return referenceId; }
    public String getEgmId() { return egmId; }
    public Long getBalanceAfter() { return balanceAfter; }
    public String getTransBy() { return transBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
