package com.dustwing.policy;

public class PolicyEnforcerException extends Exception{
    public PolicyEnforcerException() {
        super();
    }

    public PolicyEnforcerException(String message) {
        super(message);
    }

    public PolicyEnforcerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyEnforcerException(Throwable cause) {
        super(cause);
    }
}
