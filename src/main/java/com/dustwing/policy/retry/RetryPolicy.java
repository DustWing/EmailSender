package com.dustwing.policy.retry;

import com.dustwing.result.Failure;
import com.dustwing.result.Result;
import com.dustwing.result.Success;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);

    private final TimeUnit timeUnit;

    private final long delay;

    private final int maxRetries;
    private final List<Class<? extends Exception>> handle;

    public RetryPolicy(TimeUnit timeUnit, long delay, int maxRetries, List<Class<? extends Exception>> handle) {
        this.timeUnit = timeUnit;
        this.delay = delay;
        this.maxRetries = maxRetries;
        this.handle = handle;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getDelay() {
        return delay;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public List<Class<? extends Exception>> getHandle() {
        return handle;
    }


    public static <T> Result<T> retry(RetryPolicy retryPolicy, Function<T, Result<T>> f, T t, Exception ex) {


        if (!handle(retryPolicy, ex)) {
            return new Failure<>(t, ex);
        }


        if (retryPolicy.getMaxRetries() == 0) {
            return retryForEver(retryPolicy, f, t);
        }

        return retryMaxTries(retryPolicy, f, t);

    }


    private static <T> Result<T> retryMaxTries(RetryPolicy retryPolicy, Function<T, Result<T>> f, T t) {

        int tries = 0;
        while (tries < retryPolicy.getMaxRetries()) {

            //delay
            try {
                retryPolicy.getTimeUnit().sleep(retryPolicy.getDelay());
            } catch (InterruptedException e) {
                return new Failure<>(t, new RetryPolicyException("Retry policy delay exception", e));
            }

            logger.debug("retrying..");
            tries++;

            var result = f.apply(t);
            if (result instanceof Success<T> success) {
                return success;
            } else if (result instanceof Failure<T> failure) {
                logger.error("Error in retry: ", failure.exception());
                if (!handle(retryPolicy, failure.exception())) {
                    return failure;
                }
            }
        }

        return new Failure<>(t, new RetryPolicyException("Failure: Max retries reached..."));
    }

    private static <T> Result<T> retryForEver(RetryPolicy retryPolicy, Function<T, Result<T>> f, T t) {

        while (true) {
            try {
                retryPolicy.getTimeUnit().sleep(retryPolicy.getDelay());
            } catch (InterruptedException e) {
                return new Failure<>(t, new RetryPolicyException("Retry policy delay exception", e));
            }

            logger.debug("retrying..");

            var result = f.apply(t);
            if (result instanceof Success<T> success) {
                return success;
            } else if (result instanceof Failure<T> failure) {
                logger.error("Error in retry: ", failure.exception());
                if (!handle(retryPolicy, failure.exception())) {
                    return failure;
                }
            }
        }

    }


    private static boolean handle(RetryPolicy retryPolicy, Exception ex) {

        if (retryPolicy.getHandle() == null || retryPolicy.getHandle().isEmpty()) {
            return false;
        }

        boolean found = retryPolicy.getHandle().stream().anyMatch(ex.getClass()::equals);

        if (!found) {
            return false;
        }

        logger.debug("Handling exception" + ex.getMessage());

        return true;
    }

    public static RetryPolicyBuilder builder() {
        return new RetryPolicyBuilder();
    }


    public static class RetryPolicyBuilder {
        private TimeUnit timeUnit;
        private long delay;
        private int maxRetries;
        private List<Class<? extends Exception>> handle;

        public RetryPolicyBuilder withDelay(TimeUnit timeUnit, long delay) {
            this.timeUnit = timeUnit;
            this.delay = delay;
            return this;
        }

        public RetryPolicyBuilder withMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryPolicyBuilder handle(List<Class<? extends Exception>> handle) {
            this.handle = handle;
            return this;
        }

        public RetryPolicy build() {

            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries cannot be negative");
            }

            return new RetryPolicy(timeUnit, delay, maxRetries, handle);
        }
    }
}
