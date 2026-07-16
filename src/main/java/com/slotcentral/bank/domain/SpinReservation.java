package com.slotcentral.bank.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "spin_reservations")
public class SpinReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spin_id", nullable = false, unique = true)
    private String spinId;

    @Column(name = "account_uid", nullable = false)
    private String accountUid;

    @Column(name = "reserved_amount", nullable = false)
    private Long reservedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.RESERVED;

    @Column(name = "balance_after_reserve")
    private Long balanceAfterReserve;

    @Column(name = "balance_after_settle")
    private Long balanceAfterSettle;

    @Column(name = "win_amount")
    private Long winAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getSpinId() { return spinId; }
    public void setSpinId(String spinId) { this.spinId = spinId; }
    public String getAccountUid() { return accountUid; }
    public void setAccountUid(String accountUid) { this.accountUid = accountUid; }
    public Long getReservedAmount() { return reservedAmount; }
    public void setReservedAmount(Long reservedAmount) { this.reservedAmount = reservedAmount; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public Long getBalanceAfterReserve() { return balanceAfterReserve; }
    public void setBalanceAfterReserve(Long balanceAfterReserve) { this.balanceAfterReserve = balanceAfterReserve; }
    public Long getBalanceAfterSettle() { return balanceAfterSettle; }
    public void setBalanceAfterSettle(Long balanceAfterSettle) { this.balanceAfterSettle = balanceAfterSettle; }
    public Long getWinAmount() { return winAmount; }
    public void setWinAmount(Long winAmount) { this.winAmount = winAmount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
