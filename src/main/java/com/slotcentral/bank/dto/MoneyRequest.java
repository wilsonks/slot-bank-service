package com.slotcentral.bank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MoneyRequest {
    @NotNull @Min(1)
    private Long amount;

    @NotBlank
    private String referenceId;

    private String transBy;
    private String egmId;

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getTransBy() { return transBy; }
    public void setTransBy(String transBy) { this.transBy = transBy; }
    public String getEgmId() { return egmId; }
    public void setEgmId(String egmId) { this.egmId = egmId; }
}
