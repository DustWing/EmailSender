package com.dustwing;

import com.dustwing.policy.PolicyEnforcer;
import com.dustwing.policy.retry.RetryPolicy;
import com.dustwing.policy.validate.SpamPolicy;
import com.dustwing.result.Failure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;


public class PolicyEnforcerTest {


    @Test
    void test() {
        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from("fromEmail")
                .subject("PolicyEnforcerTest")
                .body("PolicyEnforcerTest")
                .html(true)
                .to(List.of(""))
                .build();


        var client = new EmailClientError();


        RetryPolicy retryPolicy = RetryPolicy.builder()
                .withDelay(TimeUnit.SECONDS, 1)
                .withMaxRetries(3)
                .handle(List.of(EmailException.class))
                .build();


        SpamPolicy spamPolicy = new SpamPolicy(Duration.ofSeconds(4));

        PolicyEnforcer<EmailNotification> enforcer = PolicyEnforcer.<EmailNotification, Boolean>builder()
                .withValidations(List.of(spamPolicy))
                .retry(retryPolicy)
                .build();


        assertThrowsExactly(
                RuntimeException.class,
                () -> enforcer.run(
                        client::send, notification
                )
        );

    }

    @Test
    void testAsync() {
        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from("fromEmail")
                .subject("PolicyEnforcerTestAsync")
                .body("PolicyEnforcerTestAsync")
                .html(true)
                .to(List.of(""))
                .build();


        var client = new EmailClientPass();


        RetryPolicy retryPolicy = RetryPolicy.builder()
                .withDelay(TimeUnit.SECONDS, 1)
                .withMaxRetries(3)
                .handle(List.of(EmailException.class))
                .build();


        SpamPolicy spamPolicy = new SpamPolicy(Duration.ofSeconds(4));


        PolicyEnforcer<EmailNotification> enforcer = PolicyEnforcer.<EmailNotification, Boolean>builder()
                .withValidations(List.of(spamPolicy))
                .retry(retryPolicy)
                .build();


        try {
            var result = enforcer.runAsync(client::send, notification)
                    .get();

            if (result instanceof Failure<EmailNotification> failure) {
                fail(failure.exception());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }


    @Test
    void testFallback() {
        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from("fromEmail")
                .subject("PolicyEnforcerTest")
                .body("PolicyEnforcerTest")
                .html(true)
                .to(List.of(""))
                .build();


        var client = new EmailClientError();

        var clientPass = new EmailClientPass();

        RetryPolicy retryPolicy = RetryPolicy.builder()
                .withDelay(TimeUnit.SECONDS, 1)
                .withMaxRetries(3)
                .handle(List.of(EmailException.class))
                .build();


        SpamPolicy spamPolicy = new SpamPolicy(Duration.ofSeconds(4));


        PolicyEnforcer<EmailNotification> enforcer = PolicyEnforcer.<EmailNotification, Boolean>builder()
                .withValidations(List.of(spamPolicy))
                .retry(retryPolicy)
                .withFallBack(List.of(client::send, clientPass::send))
                .build();


        var result = enforcer.run(client::send, notification);
        if (result instanceof Failure<EmailNotification> failure) {
            fail(failure.exception());
        }

    }
}
