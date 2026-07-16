package com.slotcentral.bank.integration;

import com.slotcentral.bank.config.TestSecurityConfig;
import com.slotcentral.bank.domain.ReservationStatus;
import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class BankIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AccountService accountService;

    private MoneyRequest moneyReq(long amount, String refId) {
        MoneyRequest r = new MoneyRequest();
        r.setAmount(amount);
        r.setReferenceId(refId);
        return r;
    }

    @Test
    void reserveAndSettle_fullFlow() {
        accountService.deposit("player-int-1", moneyReq(1000L, "dep-1"));

        BetReserveRequest reserveReq = new BetReserveRequest();
        reserveReq.setSpinId("spin-int-1");
        reserveReq.setAccountUid("player-int-1");
        reserveReq.setBetAmount(100L);
        BetReservationResponse reserved = accountService.reserveBet(reserveReq);
        assertThat(reserved.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reserved.getBalanceAfterReserve()).isEqualTo(900L);

        BetSettleRequest settleReq = new BetSettleRequest();
        settleReq.setSpinId("spin-int-1");
        settleReq.setWinAmount(200L);
        BetReservationResponse settled = accountService.settleBet(settleReq);
        assertThat(settled.getStatus()).isEqualTo(ReservationStatus.SETTLED);
        assertThat(settled.getBalanceAfterSettle()).isEqualTo(1100L);

        AccountResponse balance = accountService.getBalance("player-int-1");
        assertThat(balance.getBalance()).isEqualTo(1100L);
    }

    @Test
    void reserveAndRefund_fullFlow() {
        accountService.deposit("player-int-2", moneyReq(500L, "dep-2"));

        BetReserveRequest reserveReq = new BetReserveRequest();
        reserveReq.setSpinId("spin-int-2");
        reserveReq.setAccountUid("player-int-2");
        reserveReq.setBetAmount(100L);
        accountService.reserveBet(reserveReq);

        BetReservationResponse refunded = accountService.refundBet("spin-int-2");
        assertThat(refunded.getStatus()).isEqualTo(ReservationStatus.REFUNDED);

        AccountResponse balance = accountService.getBalance("player-int-2");
        assertThat(balance.getBalance()).isEqualTo(500L);
    }

    @Test
    void ledgerQuery_filtersAndPaginates() {
        accountService.deposit("player-int-3", moneyReq(1000L, "dep-3a"));
        accountService.deposit("player-int-3", moneyReq(500L, "dep-3b"));

        Page<LedgerEntryResponse> page = accountService.getLedgerForAccount(
                "player-int-3", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(e -> e.getAccountUid().equals("player-int-3"));
    }

    @Test
    void duplicateReserve_idempotent() {
        accountService.deposit("player-int-4", moneyReq(1000L, "dep-4"));

        BetReserveRequest req = new BetReserveRequest();
        req.setSpinId("spin-int-4");
        req.setAccountUid("player-int-4");
        req.setBetAmount(100L);

        BetReservationResponse first = accountService.reserveBet(req);
        BetReservationResponse second = accountService.reserveBet(req);

        assertThat(first.getSpinId()).isEqualTo(second.getSpinId());
        AccountResponse balance = accountService.getBalance("player-int-4");
        assertThat(balance.getBalance()).isEqualTo(900L);
    }

    @Test
    void duplicateSettle_idempotent() {
        accountService.deposit("player-int-5", moneyReq(1000L, "dep-5"));

        BetReserveRequest reserveReq = new BetReserveRequest();
        reserveReq.setSpinId("spin-int-5");
        reserveReq.setAccountUid("player-int-5");
        reserveReq.setBetAmount(100L);
        accountService.reserveBet(reserveReq);

        BetSettleRequest settleReq = new BetSettleRequest();
        settleReq.setSpinId("spin-int-5");
        settleReq.setWinAmount(50L);

        BetReservationResponse first = accountService.settleBet(settleReq);
        BetReservationResponse second = accountService.settleBet(settleReq);

        assertThat(first.getStatus()).isEqualTo(ReservationStatus.SETTLED);
        assertThat(second.getStatus()).isEqualTo(ReservationStatus.SETTLED);
    }
}
