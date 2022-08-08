package com.dustwing.queue;

import com.dustwing.EmailNotification;
import com.dustwing.IEmailSender;
import com.dustwing.result.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmailBlockingQueue implements IEmailSenderQueue<EmailNotification> {

    private final static Logger sLogger = LoggerFactory.getLogger(EmailBlockingQueue.class);

    private final BlockingQueue<QueueItem> queue;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public static EmailBlockingQueue create() {

        return new EmailBlockingQueue(
                new LinkedBlockingQueue<>()
        );
    }

    public EmailBlockingQueue(
            BlockingQueue<QueueItem> queue
    ) {
        this.queue = queue;

    }

    @Override
    public void add(IEmailSender<EmailNotification> emailSender, EmailNotification notification) {

        this.queue.add(
                new QueueItem(
                        emailSender,
                        notification
                )
        );
    }

    @Override
    public void start() {

        executorService.submit(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {

                        if (!isRunning.get()) {
                            continue;
                        }
                        try {

                            final QueueItem item = queue.take();

                            var result = item.emailSender().send(item.notification());

                            if (result instanceof Failure<EmailNotification> failure) {
                                sLogger.error("Failure in Email queue:", failure.exception());
                            }

                        } catch (InterruptedException e) {
                            sLogger.error("Queue interrupted", e);
                            break;
                        } catch (Exception ex) {
                            sLogger.error("Exception in Email queue: ", ex);
                        }

                    }
                }
        );
    }

    @Override
    public void resume() {
        isRunning.set(true);
    }

    @Override
    public void pause() {
        isRunning.set(false);
    }

    @Override
    public void shutdown() {
        sLogger.debug("Shutting down....");
        executorService.shutdown();
    }


    private record QueueItem(IEmailSender<EmailNotification> emailSender, EmailNotification notification) {

    }


}
