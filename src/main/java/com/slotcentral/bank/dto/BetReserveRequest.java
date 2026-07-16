package com.slotcentral.bank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BetReserveRequest {
    @NotBlank
    private String spinId;

    @NotBlank
    private String accountUid;

    @NotNull @Min(1)
    private Long betAmount;

    private String egmId;

    public String getSpinId() { return spinId; }
    public void setSpinId(String spinId) { this.spinId = spinId; }
    public String getAccountUid() { return accountUid; }
    public void setAccountUid(String accountUid) { this.accountUid = accountUid; }
    public Long getBetAmount() { return betAmount; }
    public void setBetAmount(Long betAmount) { this.betAmount = betAmount; }
    public String getEgmId() { return egmId; }
    public void setEgmId(String egmId) { this.egmId = egmId; }
}
