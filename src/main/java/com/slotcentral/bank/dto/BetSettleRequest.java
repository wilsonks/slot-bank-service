package com.slotcentral.bank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BetSettleRequest {
    @NotBlank
    private String spinId;

    @NotNull @Min(0)
    private Long winAmount;

    public String getSpinId() { return spinId; }
    public void setSpinId(String spinId) { this.spinId = spinId; }
    public Long getWinAmount() { return winAmount; }
    public void setWinAmount(Long winAmount) { this.winAmount = winAmount; }
}
