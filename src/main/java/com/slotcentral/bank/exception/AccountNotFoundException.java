package com.slotcentral.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String uid) { super("Account not found for uid: " + uid); }
}
