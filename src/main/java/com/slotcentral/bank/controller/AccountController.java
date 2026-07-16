package com.slotcentral.bank.controller;

import com.slotcentral.bank.domain.EntryType;
import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{uid}")
    public ResponseEntity<AccountResponse> getBalance(@PathVariable String uid) {
        return ResponseEntity.ok(accountService.getBalance(uid));
    }

    @PostMapping("/{uid}/deposit")
    public ResponseEntity<LedgerEntryResponse> deposit(
            @PathVariable String uid,
            @Valid @RequestBody MoneyRequest req) {
        return ResponseEntity.ok(accountService.deposit(uid, req));
    }

    @PostMapping("/{uid}/withdraw")
    public ResponseEntity<LedgerEntryResponse> withdraw(
            @PathVariable String uid,
            @Valid @RequestBody MoneyRequest req) {
        return ResponseEntity.ok(accountService.withdraw(uid, req));
    }

    @PostMapping("/{uid}/buy-in")
    public ResponseEntity<LedgerEntryResponse> buyIn(
            @PathVariable String uid,
            @Valid @RequestBody MoneyRequest req) {
        return ResponseEntity.ok(accountService.buyIn(uid, req));
    }

    @PostMapping("/{uid}/buy-out")
    public ResponseEntity<LedgerEntryResponse> buyOut(
            @PathVariable String uid,
            @Valid @RequestBody MoneyRequest req) {
        return ResponseEntity.ok(accountService.buyOut(uid, req));
    }

    @GetMapping("/{uid}/ledger")
    public ResponseEntity<Page<LedgerEntryResponse>> getLedger(
            @PathVariable String uid,
            @RequestParam(required = false) EntryType entryType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(accountService.getLedgerForAccount(uid, entryType, from, to, pageable));
    }
}
