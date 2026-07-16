package com.slotcentral.bank.dto;

import java.time.OffsetDateTime;

public class AccountResponse {
    private String playerUid;
    private Long balance;
    private OffsetDateTime updatedAt;

    public AccountResponse(String playerUid, Long balance, OffsetDateTime updatedAt) {
        this.playerUid = playerUid;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public String getPlayerUid() { return playerUid; }
    public Long getBalance() { return balance; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
