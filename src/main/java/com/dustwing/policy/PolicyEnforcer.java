package com.dustwing.policy;

import com.dustwing.policy.retry.RetryPolicy;
import com.dustwing.policy.validate.IValidatePolicy;
import com.dustwing.result.Failure;
import com.dustwing.result.Result;
import com.dustwing.result.Success;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.dustwing.policy.retry.RetryPolicy.retry;


public class PolicyEnforcer<T> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyEnforcer.class);

    private final List<IValidatePolicy<T>> validatePolicies;

    private final RetryPolicy retryPolicy;

    private final List<Function<T, Result<T>>> fallBack;

    private PolicyEnforcer(
            List<IValidatePolicy<T>> validatePolicies,
            RetryPolicy retryPolicy,
            List<Function<T, Result<T>>> fallBack
    ) {
        this.validatePolicies = validatePolicies;
        this.retryPolicy = retryPolicy;
        this.fallBack = fallBack;
    }


    public Result<T> run(
            Function<T, Result<T>> f, T t
    ) {

        if (validatePolicies != null) {
            var result = validate(t);
            if (result instanceof Failure<T> failure) return failure;
        }


        var result = f.apply(t);
        if (result instanceof Success<T> success) {
            return success;

        } else if (result instanceof Failure<T> failure) {

            if (retryPolicy == null) {
                return failure;
            }

            var resultRe = retry(retryPolicy, f, t, failure.exception());

            if (resultRe instanceof Success<T> successRe) {
                return successRe;

            } else if (resultRe instanceof Failure<T> failureRe) {
                if (fallBack != null) return runFallback(t);
                return failureRe;

            }

        }

        throw new RuntimeException("Could not handle Result state.");

    }

    public CompletableFuture<Result<T>> runAsync(
            Function<T, Result<T>> f, T t
    ) {

        return CompletableFuture.supplyAsync(() ->
                run(f, t)
        );

    }


    private Result<T> validate(final T t) {


        for (IValidatePolicy<T> iValidatePolicy : validatePolicies) {

            var result = iValidatePolicy.validate(t);
            if (result instanceof Failure<T> failure) {
                return failure;
            }

        }

        return new Success<>(t);

    }

    private Result<T> runFallback(T t) {


        logger.info("Starting fallback ...");

        for (Function<T, Result<T>> trFunction : fallBack) {

            var result = trFunction.apply(t);
            if (result instanceof Success<T> success) {
                return success;
            } else {
                logger.error("Fall Back failure index[{}]", fallBack.indexOf(trFunction));

            }
        }

        return new Failure<>(t, new PolicyEnforcerException("All fallback methods failed..."));
    }

    public static <T, R> PolicyEnforcerBuilder<T, R> builder() {
        return new PolicyEnforcerBuilder<>();
    }

    public static class PolicyEnforcerBuilder<T, R> {
        private List<IValidatePolicy<T>> validatePolicies;
        private RetryPolicy retryPolicy;
        private List<Function<T, Result<T>>> fallBack;

        public PolicyEnforcerBuilder<T, R> withValidations(List<IValidatePolicy<T>> validatePolicies) {
            this.validatePolicies = validatePolicies;
            return this;
        }

        public PolicyEnforcerBuilder<T, R> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public PolicyEnforcerBuilder<T, R> withFallBack(List<Function<T, Result<T>>> fallBack) {
            this.fallBack = fallBack;
            return this;
        }

        public PolicyEnforcer<T> build() {
            return new PolicyEnforcer<>(validatePolicies, retryPolicy, fallBack);
        }
    }


}
