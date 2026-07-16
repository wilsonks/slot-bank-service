package com.slotcentral.bank.dto;

import com.slotcentral.bank.domain.ReservationStatus;
import com.slotcentral.bank.domain.SpinReservation;
import java.time.OffsetDateTime;

public class BetReservationResponse {
    private String spinId;
    private String accountUid;
    private Long reservedAmount;
    private ReservationStatus status;
    private Long balanceAfterReserve;
    private Long balanceAfterSettle;
    private Long winAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static BetReservationResponse from(SpinReservation r) {
        BetReservationResponse resp = new BetReservationResponse();
        resp.spinId = r.getSpinId();
        resp.accountUid = r.getAccountUid();
        resp.reservedAmount = r.getReservedAmount();
        resp.status = r.getStatus();
        resp.balanceAfterReserve = r.getBalanceAfterReserve();
        resp.balanceAfterSettle = r.getBalanceAfterSettle();
        resp.winAmount = r.getWinAmount();
        resp.createdAt = r.getCreatedAt();
        resp.updatedAt = r.getUpdatedAt();
        return resp;
    }

    public String getSpinId() { return spinId; }
    public String getAccountUid() { return accountUid; }
    public Long getReservedAmount() { return reservedAmount; }
    public ReservationStatus getStatus() { return status; }
    public Long getBalanceAfterReserve() { return balanceAfterReserve; }
    public Long getBalanceAfterSettle() { return balanceAfterSettle; }
    public Long getWinAmount() { return winAmount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
