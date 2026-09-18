package com.gsir.files.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingAndCountingInputStream extends FilterInputStream {

    private final MessageDigest digest;
    private long byteCount = 0;

    public HashingAndCountingInputStream(InputStream in) {
        super(in);
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            digest.update((byte) b);
            byteCount++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        if (bytesRead != -1) {
            digest.update(b, off, bytesRead);
            byteCount += bytesRead;
        }
        return bytesRead;
    }

    public String getChecksumSha256() {
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public long getByteCount() {
        return byteCount;
    }
}
