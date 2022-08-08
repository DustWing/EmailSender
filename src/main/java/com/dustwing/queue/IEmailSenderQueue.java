package com.dustwing.queue;

import com.dustwing.EmailNotification;
import com.dustwing.IEmailSender;

public interface IEmailSenderQueue<T> {
    void add(IEmailSender<T> emailSender, EmailNotification notification);

    void start();

    void resume();

    void pause();

    void shutdown();
}
