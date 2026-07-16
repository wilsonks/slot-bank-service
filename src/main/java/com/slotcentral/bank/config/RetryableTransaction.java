package com.slotcentral.bank.config;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.*;

/** Composed annotation: retries up to 3 times with 50 ms base delay (×2) on optimistic lock conflict. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
           maxAttempts = 3,
           backoff = @Backoff(delay = 50, multiplier = 2))
public @interface RetryableTransaction {
}
