package com.dustwing;

import com.dustwing.result.Result;

public interface IEmailSender<T> {
    Result<T> send(T t);
}
