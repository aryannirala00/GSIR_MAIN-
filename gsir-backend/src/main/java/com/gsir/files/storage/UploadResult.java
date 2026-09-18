package com.gsir.files.storage;

public record UploadResult(String checksumSha256, long sizeBytes) {}
