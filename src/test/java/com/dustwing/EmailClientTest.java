package com.dustwing;

import com.dustwing.result.Failure;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.dustwing.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class EmailClientTest {


    @Test
    void testWithSsl() {

        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from(fromEmail)
                .subject("testWithSsl")
                .body("testWithSsl")
                .html(true)
                .to(toRecipients)
                .build();


        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        var client = EmailSender.create(props, userName, password);

        var result = client.send(notification);
        if (result instanceof Failure<EmailNotification> failure) {
            fail(failure.exception());
        }

    }

    @Test
    void testWithCCAndBCC() {
        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from(fromEmail)
                .subject("testWithSsl")
                .body("testWithSsl")
                .html(true)
                .to(toRecipients)
                .cc(ccRecipients)
                .bcc(bccRecipients)
                .build();


        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        var client = EmailSender.create(props, userName, password);

        var result = client.send(notification);
        if (result instanceof Failure<EmailNotification> failure) {
            fail(failure.exception());
        }
    }

    @Test
    void testAsync() {

        EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                .from(fromEmail)
                .subject("testAsync")
                .body("testAsync")
                .to(toRecipients)
                .build();


        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        var client = EmailSender.create(props, userName, password);

        try {
            var result = CompletableFuture.supplyAsync(() -> client.send(notification))
                    .get();
            if (result instanceof Failure<EmailNotification> failure) {
                fail(failure.exception());
            }
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }


    }

    @Test
    void testWithoutFromEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailNotification.EmailNotificationBuilder()
                        .subject("testWithSsl")
                        .body("testWithSsl")
                        .to(toRecipients)
                        .build()
        );
    }

    @Test
    void testWithoutSubject() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailNotification.EmailNotificationBuilder()
                        .from(fromEmail)
                        .body("testWithSsl")
                        .to(toRecipients)
                        .build()
        );
    }

    @Test
    void testWithoutBody() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailNotification.EmailNotificationBuilder()
                        .from(fromEmail)
                        .subject("testWithSsl")
                        .to(toRecipients)
                        .build()
        );
    }

    @Test
    void testWithoutRecipients() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailNotification.EmailNotificationBuilder()
                        .from(fromEmail)
                        .subject("testWithSsl")
                        .body("testWithSsl")
                        .build()
        );
    }

    @Test
    void testWithTls() {

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        var client = EmailSender.create(props, userName, password);

        var notification = new EmailNotification.EmailNotificationBuilder()
                .from(fromEmail)
                .subject("testWithTls")
                .body("testWithTls")
                .to(toRecipients)
                .build();

        var result = client.send(notification);
        if (result instanceof Failure<EmailNotification> failure) {
            fail(failure.exception());
        }

    }

    @Test
    void TestWithImages() {
        try (InputStream is = EmailClientError.class.getClassLoader().getResourceAsStream("template/index.html")) {

            if (is == null) {
                throw new RuntimeException("no file found");
            }

            final String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            final String image1Path = "";
            final String image2Path = "";
            final String image3Path = "";
            final String image4Path = "";
            final String image5Path = "";


            final File image1 = new File(image1Path);
            final File image2 = new File(image2Path);
            final File image3 = new File(image3Path);
            final File image4 = new File(image4Path);
            final File image5 = new File(image5Path);

            Map<String, File> imageMap = new HashMap<>();

            imageMap.put("image1", image1);
            imageMap.put("image2", image2);
            imageMap.put("image3", image3);
            imageMap.put("image4", image4);
            imageMap.put("image5", image5);

            EmailNotification notification = new EmailNotification.EmailNotificationBuilder()
                    .from(fromEmail)
                    .subject("testWithSsl")
                    .body(html)
                    .html(true)
                    .to(toRecipients)
                    .images(imageMap)
                    .build();


            Properties props = new Properties();

            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            var client = EmailSender.create(props, userName, password);

            var result = client.send(notification);
            if (result instanceof Failure<EmailNotification> failure) {
                fail(failure.exception());
            }

        } catch (Exception ex) {
            fail(ex);
        }
    }

}