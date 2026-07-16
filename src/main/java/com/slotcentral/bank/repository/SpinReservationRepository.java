package com.slotcentral.bank.repository;

import com.slotcentral.bank.domain.SpinReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpinReservationRepository extends JpaRepository<SpinReservation, Long> {
    Optional<SpinReservation> findBySpinId(String spinId);
}
