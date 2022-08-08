package com.dustwing.policy.retry;

public class RetryPolicyException extends Exception{
    public RetryPolicyException() {
        super();
    }

    public RetryPolicyException(String message) {
        super(message);
    }

    public RetryPolicyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryPolicyException(Throwable cause) {
        super(cause);
    }
}
