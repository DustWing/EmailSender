package com.dustwing.result;

public record Success<T>(T value) implements Result<T> {
    @Override
    public T get() {
        return null;
    }
}
