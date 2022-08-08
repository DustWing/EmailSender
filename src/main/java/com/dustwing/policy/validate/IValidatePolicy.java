package com.dustwing.policy.validate;

import com.dustwing.result.Result;

public interface IValidatePolicy <T>{

    Result<T> validate(T t);


}
