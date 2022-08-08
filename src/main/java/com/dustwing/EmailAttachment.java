package com.dustwing;

public record EmailAttachment(String fileName, byte[] content, String mimeType) {


}
