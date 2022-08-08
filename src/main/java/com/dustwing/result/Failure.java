package com.dustwing.result;

public record Failure<T>(T value, Exception exception) implements Result<T> {
    @Override
    public T get() {
        return null;
    }
}
