package com.dustwing;

import com.dustwing.result.Result;
import com.dustwing.result.Success;

public class EmailClientPass implements IEmailSender<EmailNotification> {


    @Override
    public Result<EmailNotification> send(final EmailNotification emailNotification) {

        return new Success<>(emailNotification);

    }

}
