package com.dustwing.result;

public sealed interface Result<T> permits Failure, Success {

    T get();

}
