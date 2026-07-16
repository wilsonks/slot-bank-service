package com.slotcentral.bank.controller;

import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bets")
public class BetController {

    private final AccountService accountService;

    public BetController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<BetReservationResponse> reserve(
            @Valid @RequestBody BetReserveRequest req) {
        return ResponseEntity.ok(accountService.reserveBet(req));
    }

    @PostMapping("/settle")
    public ResponseEntity<BetReservationResponse> settle(
            @Valid @RequestBody BetSettleRequest req) {
        return ResponseEntity.ok(accountService.settleBet(req));
    }

    @PostMapping("/{spinId}/refund")
    public ResponseEntity<BetReservationResponse> refund(@PathVariable String spinId) {
        return ResponseEntity.ok(accountService.refundBet(spinId));
    }
}
