package com.slotcentral.bank.exception;

public class SpinReservationNotFoundException extends RuntimeException {
    public SpinReservationNotFoundException(String spinId) {
        super("SpinReservation not found for spinId: " + spinId);
    }
}
