package com.slotcentral.bank.controller;

import com.slotcentral.bank.domain.EntryType;
import com.slotcentral.bank.dto.LedgerEntryResponse;
import com.slotcentral.bank.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final AccountService accountService;

    public LedgerController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<Page<LedgerEntryResponse>> getLedger(
            @RequestParam(required = false) String accountUid,
            @RequestParam(required = false) String egmId,
            @RequestParam(required = false) EntryType entryType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(accountService.getLedger(accountUid, egmId, entryType, from, to, pageable));
    }
}
