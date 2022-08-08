package com.dustwing.policy.validate;

import com.dustwing.EmailNotification;
import com.dustwing.result.Failure;
import com.dustwing.result.Result;
import com.dustwing.result.Success;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spam policy can be shared in multiple IEmailSender
 */
public class SpamPolicy implements IValidatePolicy<EmailNotification> {

    private final Map<String, LocalDateTime> previousSubject;

    private final Duration coolDown;

    public SpamPolicy(Duration coolDown) {
        this.previousSubject = new ConcurrentHashMap<>();
        this.coolDown = coolDown;

    }

    @Override
    public Result<EmailNotification> validate(EmailNotification email) {

        //get the previous run
        final LocalDateTime previousRun = previousSubject.get(email.subject());
        final LocalDateTime now = LocalDateTime.now();

        //1st time
        if (previousRun == null) {
            previousSubject.put(email.subject(), now);
            return new Success<>(email);
        }

        //check if its still on cool down
        if (now.isBefore(previousRun.plus(coolDown))) {
            return new Failure<>(email, new ValidationException(""));
        }

        //update
        previousSubject.put(email.subject(), now);
        return new Success<>(email);
    }


}
