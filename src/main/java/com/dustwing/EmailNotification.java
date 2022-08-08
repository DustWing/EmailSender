package com.dustwing;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record EmailNotification(
        String id,
        String from,
        String subject,
        String body,
        boolean isHtml,
        Collection<EmailAttachment> attachments,
        Collection<String> toRecipients,
        Collection<String> ccRecipients,
        Collection<String> bccRecipients,
        Map<String, File> images
) {

    public EmailNotification {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("fromEmail cannot be null or blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject cannot be null or blank");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body cannot be null or blank");
        }
        if (toRecipients == null || toRecipients.isEmpty()) {
            throw new IllegalArgumentException("toRecipients cannot be null or empty");
        }

        if (!isHtml && (images == null || images.isEmpty())) {
            throw new IllegalArgumentException("Cannot create email with images without html body. Set isHtml = true.");
        }

    }


    public static EmailNotificationBuilder builder() {
        return new EmailNotificationBuilder();
    }

    public static class EmailNotificationBuilder {
        private String id;
        private String from;
        private String subject;
        private String body;
        private boolean isHtml;
        private Collection<EmailAttachment> attachments;
        private Collection<String> toRecipients;
        private Collection<String> ccRecipients;
        private Collection<String> bccRecipients;
        private Map<String, File> images;

        public EmailNotificationBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EmailNotificationBuilder from(String from) {
            this.from = from;
            return this;
        }

        public EmailNotificationBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailNotificationBuilder body(String body) {
            this.body = body;
            return this;
        }

        public EmailNotificationBuilder html(boolean isHtml) {
            this.isHtml = isHtml;
            return this;
        }

        public EmailNotificationBuilder attachments(Collection<EmailAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public EmailNotificationBuilder to(Collection<String> toRecipients) {
            this.toRecipients = toRecipients;
            return this;
        }

        public EmailNotificationBuilder cc(Collection<String> ccRecipients) {
            this.ccRecipients = ccRecipients;
            return this;
        }

        public EmailNotificationBuilder bcc(Collection<String> bccRecipients) {
            this.bccRecipients = bccRecipients;
            return this;
        }

        public EmailNotificationBuilder images(Map<String, File> images) {
            this.images = images;
            return this;
        }


        public EmailNotification build() {
            return new EmailNotification(id, from, subject, body, isHtml, attachments, toRecipients, ccRecipients, bccRecipients, images);
        }
    }

}
