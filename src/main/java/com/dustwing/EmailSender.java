package com.dustwing;

import com.dustwing.result.Failure;
import com.dustwing.result.Result;
import com.dustwing.result.Success;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EmailSender implements IEmailSender<EmailNotification> {

    final Session session;

    public static EmailSender create(
            final Properties properties,
            final String user,
            final String password
    ) {


        final Session session = Session.getDefaultInstance(
                properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });

        return new EmailSender(session);
    }


    public static EmailSender create(
            final Properties properties
    ) {
        return new EmailSender(Session.getDefaultInstance(properties));
    }

    public EmailSender(
            final Session session
    ) {
        this.session = session;
    }


    @Override
    public Result<EmailNotification> send(final EmailNotification email) {

        try {

            Message message = buildMessage(email, session);

            Transport.send(message);

        } catch (MessagingException | IOException e) {
            return new Failure<>(email, e);
        }

        return new Success<>(email);

    }

    private Message buildMessage(
            final EmailNotification emailNotification, final Session session
    ) throws MessagingException, IOException {
        final boolean isHtml = emailNotification.isHtml();
        final String body = emailNotification.body();

        final MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(emailNotification.from()));


        setRecipients(message, emailNotification.toRecipients(), Message.RecipientType.TO);

        setRecipients(message, emailNotification.ccRecipients(), Message.RecipientType.CC);

        setRecipients(message, emailNotification.bccRecipients(), Message.RecipientType.BCC);

        message.setSubject(emailNotification.subject(), "UTF-8");


        //With Attachments and images
        if (emailNotification.attachments() != null && !emailNotification.attachments().isEmpty()) {

            withAttachments(emailNotification, isHtml, body, message);
        }
        //With images only
        else if (emailNotification.images() != null && !emailNotification.images().isEmpty()) {

            withHtmlInlineImages(emailNotification, body, message);

        } else {
            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }
        }
        return message;
    }

    private void withHtmlInlineImages(EmailNotification emailNotification, String body, MimeMessage message) throws MessagingException, IOException {

        // Create a multipart message
        final Multipart multipart = new MimeMultipart();

        // Create the message part
        final BodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setContent(body, "text/html; charset=utf-8");

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        addImagesInBody(multipart, emailNotification.images());

        // Send the complete message parts
        message.setContent(multipart);
    }

    private void withAttachments(EmailNotification emailNotification, boolean isHtml, String body, MimeMessage message) throws MessagingException, IOException {
        // Create a multipart message
        final Multipart multipart = new MimeMultipart();

        // Create the message part
        final BodyPart messageBodyPart = new MimeBodyPart();

        // Now set the actual message
        if (isHtml) {
            messageBodyPart.setContent(body, "text/html; charset=utf-8");
            addImagesInBody(multipart, emailNotification.images());

        } else {
            messageBodyPart.setText(body);
        }

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        addAttachments(multipart, emailNotification.attachments());

        // Send the complete message parts
        message.setContent(multipart);
    }


    private void setRecipients(final MimeMessage message, Collection<String> recipients, Message.RecipientType type) {

        if (recipients == null) {
            return;
        }

        recipients.forEach(
                e -> {
                    try {
                        message.addRecipient(type, new InternetAddress(e));
                    } catch (MessagingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );
    }

    private void addImagesInBody(final Multipart multipart, final Map<String, File> mapInlineImages) throws MessagingException, IOException {
        // adds inline image attachments

        if (mapInlineImages == null || mapInlineImages.isEmpty()) {
            return;
        }

        Set<String> setImageID = mapInlineImages.keySet();

        for (String contentId : setImageID) {
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setHeader("Content-ID", "<" + contentId + ">");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            imagePart.attachFile(mapInlineImages.get(contentId));
            multipart.addBodyPart(imagePart);
        }

    }

    private void addAttachments(final Multipart multipart, Collection<EmailAttachment> attachments) throws MessagingException {

        for (EmailAttachment att : attachments) {
            // Part two is attachment
            final BodyPart attachmentBodyPart = new MimeBodyPart();
            final DataSource source = new ByteArrayDataSource(att.content(), att.mimeType());

            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(att.fileName());

            multipart.addBodyPart(attachmentBodyPart);
        }
    }

}
