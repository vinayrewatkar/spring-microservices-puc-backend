package com.puc.rcVerificationService.utils;

public class FileData {
    private final String filename;
    private final byte[] buffer;

    public FileData(String filename, byte[] buffer) {
        this.filename = filename;
        this.buffer = buffer;
    }

    public String getFilename() { return filename; }
    public byte[] getBuffer() { return buffer; }
}