package com.slotcentral.bank.integration;

import com.slotcentral.bank.config.TestSecurityConfig;
import com.slotcentral.bank.dto.*;
import com.slotcentral.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ConcurrencyTest {

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

    @Test
    void concurrentBetReserve_noLostUpdates() throws InterruptedException {
        String playerUid = "concurrent-player-" + UUID.randomUUID();
        long initialBalance = 500L;
        long betAmount = 100L;
        int threadCount = 10;

        MoneyRequest deposit = new MoneyRequest();
        deposit.setAmount(initialBalance);
        deposit.setReferenceId("initial-deposit-" + playerUid);
        accountService.deposit(playerUid, deposit);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String spinId = "spin-concurrent-" + i + "-" + playerUid;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    BetReserveRequest req = new BetReserveRequest();
                    req.setSpinId(spinId);
                    req.setAccountUid(playerUid);
                    req.setBetAmount(betAmount);
                    accountService.reserveBet(req);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
                return null;
            }));
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long expectedSuccesses = initialBalance / betAmount;
        AccountResponse finalBalance = accountService.getBalance(playerUid);

        assertThat(successCount.get()).isEqualTo((int) expectedSuccesses);
        assertThat(finalBalance.getBalance()).isEqualTo(initialBalance - successCount.get() * betAmount);
        assertThat(failCount.get()).isEqualTo(threadCount - successCount.get());
        assertThat(futures).hasSize(threadCount);
    }
}
