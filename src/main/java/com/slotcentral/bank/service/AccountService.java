package com.slotcentral.bank.service;

import com.slotcentral.bank.domain.*;
import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.exception.*;
import com.slotcentral.bank.repository.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AccountService {
    private static final OffsetDateTime LEDGER_FROM_FALLBACK = OffsetDateTime.parse("1970-01-01T00:00:00Z");
    private static final OffsetDateTime LEDGER_TO_FALLBACK = OffsetDateTime.parse("2999-12-31T23:59:59Z");

    private final AccountRepository accountRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final SpinReservationRepository reservationRepo;

    public AccountService(AccountRepository accountRepo,
                          LedgerEntryRepository ledgerRepo,
                          SpinReservationRepository reservationRepo) {
        this.accountRepo = accountRepo;
        this.ledgerRepo = ledgerRepo;
        this.reservationRepo = reservationRepo;
    }

    @Transactional
    public Account getOrCreateAccount(String playerUid) {
        return accountRepo.findByPlayerUid(playerUid)
                .orElseGet(() -> {
                    Account a = new Account();
                    a.setPlayerUid(playerUid);
                    a.setBalance(0L);
                    return accountRepo.save(a);
                });
    }

    @Transactional(readOnly = true)
    public AccountResponse getBalance(String playerUid) {
        Account a = accountRepo.findByPlayerUid(playerUid)
                .orElseThrow(() -> new AccountNotFoundException(playerUid));
        return new AccountResponse(a.getPlayerUid(), a.getBalance(), a.getUpdatedAt());
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public LedgerEntryResponse deposit(String playerUid, MoneyRequest req) {
        Optional<LedgerEntry> existing = ledgerRepo.findByReferenceIdAndEntryType(req.getReferenceId(), EntryType.DEPOSIT);
        if (existing.isPresent()) {
            return LedgerEntryResponse.from(existing.get());
        }
        Account account = getOrCreateAccount(playerUid);
        account = accountRepo.findByPlayerUidForUpdate(playerUid).orElse(account);
        long newBalance = account.getBalance() + req.getAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(playerUid, EntryType.DEPOSIT, req.getAmount(),
                req.getReferenceId(), req.getEgmId(), newBalance, req.getTransBy());
        ledgerRepo.save(entry);
        return LedgerEntryResponse.from(entry);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public LedgerEntryResponse withdraw(String playerUid, MoneyRequest req) {
        Optional<LedgerEntry> existing = ledgerRepo.findByReferenceIdAndEntryType(req.getReferenceId(), EntryType.WITHDRAW);
        if (existing.isPresent()) {
            return LedgerEntryResponse.from(existing.get());
        }
        Account account = accountRepo.findByPlayerUidForUpdate(playerUid)
                .orElseThrow(() -> new AccountNotFoundException(playerUid));
        if (account.getBalance() < req.getAmount()) {
            throw new InsufficientBalanceException(
                "Insufficient balance: has " + account.getBalance() + ", needs " + req.getAmount());
        }
        long newBalance = account.getBalance() - req.getAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(playerUid, EntryType.WITHDRAW, -req.getAmount(),
                req.getReferenceId(), req.getEgmId(), newBalance, req.getTransBy());
        ledgerRepo.save(entry);
        return LedgerEntryResponse.from(entry);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public LedgerEntryResponse buyIn(String playerUid, MoneyRequest req) {
        Optional<LedgerEntry> existing = ledgerRepo.findByReferenceIdAndEntryType(req.getReferenceId(), EntryType.BUY_IN);
        if (existing.isPresent()) {
            return LedgerEntryResponse.from(existing.get());
        }
        Account account = getOrCreateAccount(playerUid);
        account = accountRepo.findByPlayerUidForUpdate(playerUid).orElse(account);
        long newBalance = account.getBalance() + req.getAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(playerUid, EntryType.BUY_IN, req.getAmount(),
                req.getReferenceId(), req.getEgmId(), newBalance, req.getTransBy());
        ledgerRepo.save(entry);
        return LedgerEntryResponse.from(entry);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public LedgerEntryResponse buyOut(String playerUid, MoneyRequest req) {
        Optional<LedgerEntry> existing = ledgerRepo.findByReferenceIdAndEntryType(req.getReferenceId(), EntryType.BUY_OUT);
        if (existing.isPresent()) {
            return LedgerEntryResponse.from(existing.get());
        }
        Account account = accountRepo.findByPlayerUidForUpdate(playerUid)
                .orElseThrow(() -> new AccountNotFoundException(playerUid));
        if (account.getBalance() < req.getAmount()) {
            throw new InsufficientBalanceException(
                "Insufficient balance for buy-out: has " + account.getBalance() + ", needs " + req.getAmount());
        }
        long newBalance = account.getBalance() - req.getAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(playerUid, EntryType.BUY_OUT, -req.getAmount(),
                req.getReferenceId(), req.getEgmId(), newBalance, req.getTransBy());
        ledgerRepo.save(entry);
        return LedgerEntryResponse.from(entry);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public BetReservationResponse reserveBet(BetReserveRequest req) {
        Optional<SpinReservation> existing = reservationRepo.findBySpinId(req.getSpinId());
        if (existing.isPresent()) {
            return BetReservationResponse.from(existing.get());
        }
        Account account = accountRepo.findByPlayerUidForUpdate(req.getAccountUid())
                .orElseThrow(() -> new AccountNotFoundException(req.getAccountUid()));
        if (account.getBalance() < req.getBetAmount()) {
            throw new InsufficientBalanceException(
                "Insufficient balance to reserve bet: has " + account.getBalance() + ", needs " + req.getBetAmount());
        }
        long newBalance = account.getBalance() - req.getBetAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(req.getAccountUid(), EntryType.BET_RESERVE, -req.getBetAmount(),
                req.getSpinId(), req.getEgmId(), newBalance, null);
        ledgerRepo.save(entry);
        SpinReservation reservation = new SpinReservation();
        reservation.setSpinId(req.getSpinId());
        reservation.setAccountUid(req.getAccountUid());
        reservation.setReservedAmount(req.getBetAmount());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setBalanceAfterReserve(newBalance);
        reservationRepo.save(reservation);
        return BetReservationResponse.from(reservation);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public BetReservationResponse settleBet(BetSettleRequest req) {
        SpinReservation reservation = reservationRepo.findBySpinId(req.getSpinId())
                .orElseThrow(() -> new SpinReservationNotFoundException(req.getSpinId()));
        if (reservation.getStatus() == ReservationStatus.SETTLED) {
            return BetReservationResponse.from(reservation);
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidReservationStateException(
                "Cannot settle reservation in state: " + reservation.getStatus());
        }
        Account account = accountRepo.findByPlayerUidForUpdate(reservation.getAccountUid())
                .orElseThrow(() -> new AccountNotFoundException(reservation.getAccountUid()));
        long winAmount = req.getWinAmount();
        EntryType entryType = winAmount > 0 ? EntryType.BET_SETTLE_WIN : EntryType.BET_SETTLE_NO_WIN;
        long newBalance = account.getBalance() + winAmount;
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(reservation.getAccountUid(), entryType, winAmount,
                req.getSpinId(), null, newBalance, null);
        ledgerRepo.save(entry);
        reservation.setStatus(ReservationStatus.SETTLED);
        reservation.setWinAmount(winAmount);
        reservation.setBalanceAfterSettle(newBalance);
        reservationRepo.save(reservation);
        return BetReservationResponse.from(reservation);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class,
               maxAttempts = 10, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public BetReservationResponse refundBet(String spinId) {
        SpinReservation reservation = reservationRepo.findBySpinId(spinId)
                .orElseThrow(() -> new SpinReservationNotFoundException(spinId));
        if (reservation.getStatus() == ReservationStatus.REFUNDED) {
            return BetReservationResponse.from(reservation);
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidReservationStateException(
                "Cannot refund reservation in state: " + reservation.getStatus());
        }
        Account account = accountRepo.findByPlayerUidForUpdate(reservation.getAccountUid())
                .orElseThrow(() -> new AccountNotFoundException(reservation.getAccountUid()));
        long newBalance = account.getBalance() + reservation.getReservedAmount();
        account.setBalance(newBalance);
        accountRepo.save(account);
        LedgerEntry entry = buildEntry(reservation.getAccountUid(), EntryType.BET_REFUND,
                reservation.getReservedAmount(), spinId, null, newBalance, null);
        ledgerRepo.save(entry);
        reservation.setStatus(ReservationStatus.REFUNDED);
        reservation.setBalanceAfterSettle(newBalance);
        reservationRepo.save(reservation);
        return BetReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getLedgerForAccount(String playerUid, EntryType entryType,
                                                          OffsetDateTime from, OffsetDateTime to,
                                                          Pageable pageable) {
        accountRepo.findByPlayerUid(playerUid)
                .orElseThrow(() -> new AccountNotFoundException(playerUid));
        return ledgerRepo.findWithFilters(playerUid, entryType, null, normalizeFrom(from), normalizeTo(to), pageable)
                .map(LedgerEntryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getLedger(String accountUid, String egmId, EntryType entryType,
                                                OffsetDateTime from, OffsetDateTime to,
                                                Pageable pageable) {
        return ledgerRepo.findWithFilters(accountUid, entryType, egmId, normalizeFrom(from), normalizeTo(to), pageable)
                .map(LedgerEntryResponse::from);
    }

    private OffsetDateTime normalizeFrom(OffsetDateTime from) {
        return from != null ? from : LEDGER_FROM_FALLBACK;
    }

    private OffsetDateTime normalizeTo(OffsetDateTime to) {
        return to != null ? to : LEDGER_TO_FALLBACK;
    }

    private LedgerEntry buildEntry(String accountUid, EntryType entryType, long amount,
                                   String referenceId, String egmId, long balanceAfter, String transBy) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccountUid(accountUid);
        entry.setEntryType(entryType);
        entry.setAmount(amount);
        entry.setReferenceId(referenceId);
        entry.setEgmId(egmId);
        entry.setBalanceAfter(balanceAfter);
        entry.setTransBy(transBy);
        return entry;
    }
}
