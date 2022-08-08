package com.dustwing;


import com.dustwing.queue.EmailBlockingQueue;
import com.dustwing.queue.IEmailSenderQueue;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.dustwing.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class EmailClientQueueTest {


    @Test
    void testQueue() {

        final Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        final EmailSender sender = EmailSender.create(props, userName, password);


        final IEmailSenderQueue<EmailNotification> queue = EmailBlockingQueue.create();
        queue.start();

        try {

            queue.add(sender, createNotification("1"));

            Thread.sleep(3000);

            queue.pause();

            queue.add(sender, createNotification("2"));

            queue.add(sender, createNotification("3"));

            queue.resume();

            Thread.sleep(3000);

            queue.shutdown();

            assertTrue(true);

        } catch (Throwable ex) {
            fail(ex);
        }

    }

    private static EmailNotification createNotification(String subject) {
        return new EmailNotification.EmailNotificationBuilder()
                .from(fromEmail)
                .subject("Test" + subject)
                .body("Test")
                .to(toRecipients)
                .build();
    }

}