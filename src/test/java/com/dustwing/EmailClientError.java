package com.dustwing;

import com.dustwing.result.Failure;
import com.dustwing.result.Result;

public class EmailClientError implements IEmailSender<EmailNotification> {


    @Override
    public Result<EmailNotification> send(final EmailNotification emailNotification) {

        return new Failure<>(emailNotification, new EmailException("Test exception"));

    }

}
