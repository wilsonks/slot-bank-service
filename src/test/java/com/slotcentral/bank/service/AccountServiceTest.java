package com.slotcentral.bank.service;

import com.slotcentral.bank.domain.*;
import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.exception.*;
import com.slotcentral.bank.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepo;
    @Mock private LedgerEntryRepository ledgerRepo;
    @Mock private SpinReservationRepository reservationRepo;

    @InjectMocks private AccountService service;

    private Account account;

    @BeforeEach
    void setup() {
        account = new Account();
        account.setPlayerUid("player-1");
        account.setBalance(1000L);
        account.setVersion(0L);
    }

    private MoneyRequest moneyReq(long amount, String refId) {
        MoneyRequest r = new MoneyRequest();
        r.setAmount(amount);
        r.setReferenceId(refId);
        return r;
    }

    @Test
    void deposit_increasesBalance_andCreatesEntry() {
        when(ledgerRepo.findByReferenceIdAndEntryType("ref-1", EntryType.DEPOSIT)).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        LedgerEntry saved = new LedgerEntry();
        saved.setAccountUid("player-1");
        saved.setEntryType(EntryType.DEPOSIT);
        saved.setAmount(200L);
        saved.setBalanceAfter(1200L);
        when(ledgerRepo.save(any())).thenReturn(saved);

        LedgerEntryResponse resp = service.deposit("player-1", moneyReq(200L, "ref-1"));

        assertThat(resp.getAmount()).isEqualTo(200L);
        assertThat(account.getBalance()).isEqualTo(1200L);
    }

    @Test
    void deposit_idempotency_returnsSameEntry() {
        LedgerEntry existing = new LedgerEntry();
        existing.setAmount(200L);
        existing.setEntryType(EntryType.DEPOSIT);
        existing.setBalanceAfter(1200L);
        when(ledgerRepo.findByReferenceIdAndEntryType("ref-1", EntryType.DEPOSIT)).thenReturn(Optional.of(existing));

        LedgerEntryResponse resp = service.deposit("player-1", moneyReq(200L, "ref-1"));

        assertThat(resp.getAmount()).isEqualTo(200L);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void withdraw_decreasesBalance() {
        when(ledgerRepo.findByReferenceIdAndEntryType("ref-2", EntryType.WITHDRAW)).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        LedgerEntry saved = new LedgerEntry();
        saved.setAmount(-300L);
        saved.setEntryType(EntryType.WITHDRAW);
        saved.setBalanceAfter(700L);
        when(ledgerRepo.save(any())).thenReturn(saved);

        LedgerEntryResponse resp = service.withdraw("player-1", moneyReq(300L, "ref-2"));

        assertThat(account.getBalance()).isEqualTo(700L);
        assertThat(resp.getAmount()).isEqualTo(-300L);
    }

    @Test
    void withdraw_insufficientBalance_throws() {
        when(ledgerRepo.findByReferenceIdAndEntryType(any(), eq(EntryType.WITHDRAW))).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.withdraw("player-1", moneyReq(9999L, "ref-99")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void buyIn_increasesBalance() {
        when(ledgerRepo.findByReferenceIdAndEntryType("ref-3", EntryType.BUY_IN)).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        LedgerEntry saved = new LedgerEntry();
        saved.setAmount(500L);
        saved.setEntryType(EntryType.BUY_IN);
        saved.setBalanceAfter(1500L);
        when(ledgerRepo.save(any())).thenReturn(saved);

        service.buyIn("player-1", moneyReq(500L, "ref-3"));

        assertThat(account.getBalance()).isEqualTo(1500L);
    }

    @Test
    void buyOut_decreasesBalance() {
        when(ledgerRepo.findByReferenceIdAndEntryType("ref-4", EntryType.BUY_OUT)).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        LedgerEntry saved = new LedgerEntry();
        saved.setAmount(-400L);
        saved.setEntryType(EntryType.BUY_OUT);
        saved.setBalanceAfter(600L);
        when(ledgerRepo.save(any())).thenReturn(saved);

        service.buyOut("player-1", moneyReq(400L, "ref-4"));

        assertThat(account.getBalance()).isEqualTo(600L);
    }

    @Test
    void buyOut_insufficientBalance_throws() {
        when(ledgerRepo.findByReferenceIdAndEntryType(any(), eq(EntryType.BUY_OUT))).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.buyOut("player-1", moneyReq(9999L, "ref-98")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void reserveBet_debitsAccount_createsReservation() {
        when(reservationRepo.findBySpinId("spin-1")).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        when(ledgerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BetReserveRequest req = new BetReserveRequest();
        req.setSpinId("spin-1");
        req.setAccountUid("player-1");
        req.setBetAmount(100L);

        BetReservationResponse resp = service.reserveBet(req);

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(account.getBalance()).isEqualTo(900L);
    }

    @Test
    void reserveBet_idempotency_returnsSameReservation() {
        SpinReservation existing = new SpinReservation();
        existing.setSpinId("spin-1");
        existing.setAccountUid("player-1");
        existing.setReservedAmount(100L);
        existing.setStatus(ReservationStatus.RESERVED);
        when(reservationRepo.findBySpinId("spin-1")).thenReturn(Optional.of(existing));

        BetReserveRequest req = new BetReserveRequest();
        req.setSpinId("spin-1");
        req.setAccountUid("player-1");
        req.setBetAmount(100L);

        BetReservationResponse resp = service.reserveBet(req);

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void reserveBet_insufficientBalance_throws() {
        when(reservationRepo.findBySpinId("spin-2")).thenReturn(Optional.empty());
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));

        BetReserveRequest req = new BetReserveRequest();
        req.setSpinId("spin-2");
        req.setAccountUid("player-1");
        req.setBetAmount(9999L);

        assertThatThrownBy(() -> service.reserveBet(req))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void settleBet_withWin_creditsAccount() {
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId("spin-3");
        reservation.setAccountUid("player-1");
        reservation.setReservedAmount(100L);
        reservation.setStatus(ReservationStatus.RESERVED);
        when(reservationRepo.findBySpinId("spin-3")).thenReturn(Optional.of(reservation));
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        when(ledgerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BetSettleRequest req = new BetSettleRequest();
        req.setSpinId("spin-3");
        req.setWinAmount(200L);

        BetReservationResponse resp = service.settleBet(req);

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.SETTLED);
        assertThat(resp.getWinAmount()).isEqualTo(200L);
        assertThat(account.getBalance()).isEqualTo(1200L);
    }

    @Test
    void settleBet_noWin_balanceUnchanged() {
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId("spin-4");
        reservation.setAccountUid("player-1");
        reservation.setReservedAmount(100L);
        reservation.setStatus(ReservationStatus.RESERVED);
        when(reservationRepo.findBySpinId("spin-4")).thenReturn(Optional.of(reservation));
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        when(ledgerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BetSettleRequest req = new BetSettleRequest();
        req.setSpinId("spin-4");
        req.setWinAmount(0L);

        BetReservationResponse resp = service.settleBet(req);

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.SETTLED);
        assertThat(account.getBalance()).isEqualTo(1000L);
    }

    @Test
    void settleBet_idempotency_alreadySettled() {
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId("spin-5");
        reservation.setAccountUid("player-1");
        reservation.setReservedAmount(100L);
        reservation.setStatus(ReservationStatus.SETTLED);
        reservation.setWinAmount(50L);
        when(reservationRepo.findBySpinId("spin-5")).thenReturn(Optional.of(reservation));

        BetSettleRequest req = new BetSettleRequest();
        req.setSpinId("spin-5");
        req.setWinAmount(50L);

        BetReservationResponse resp = service.settleBet(req);

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.SETTLED);
        verify(accountRepo, never()).save(any());
    }

    @Test
    void refundBet_creditsBackReservedAmount() {
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId("spin-6");
        reservation.setAccountUid("player-1");
        reservation.setReservedAmount(100L);
        reservation.setStatus(ReservationStatus.RESERVED);
        when(reservationRepo.findBySpinId("spin-6")).thenReturn(Optional.of(reservation));
        when(accountRepo.findByPlayerUidForUpdate("player-1")).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        when(ledgerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BetReservationResponse resp = service.refundBet("spin-6");

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.REFUNDED);
        assertThat(account.getBalance()).isEqualTo(1100L);
    }

    @Test
    void refundBet_idempotency_alreadyRefunded() {
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId("spin-7");
        reservation.setAccountUid("player-1");
        reservation.setReservedAmount(100L);
        reservation.setStatus(ReservationStatus.REFUNDED);
        when(reservationRepo.findBySpinId("spin-7")).thenReturn(Optional.of(reservation));

        BetReservationResponse resp = service.refundBet("spin-7");

        assertThat(resp.getStatus()).isEqualTo(ReservationStatus.REFUNDED);
        verify(accountRepo, never()).save(any());
    }
}
