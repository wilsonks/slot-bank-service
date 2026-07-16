package com.slotcentral.bank.repository;

import com.slotcentral.bank.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByPlayerUid(String playerUid);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT a FROM Account a WHERE a.playerUid = :playerUid")
    Optional<Account> findByPlayerUidForUpdate(@Param("playerUid") String playerUid);
}
