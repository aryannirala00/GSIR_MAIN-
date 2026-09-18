package com.gsir.files.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalFileSystemObjectStore implements ObjectStore {

    private final Path rootPath;

    public LocalFileSystemObjectStore(@Value("${gsir.storage.local.root-path:/tmp/gsir-storage}") String rootPathStr) throws IOException {
        this.rootPath = Paths.get(rootPathStr).toAbsolutePath().normalize();
        Files.createDirectories(this.rootPath);
    }

    private Path resolveKey(String objectKey) {
        if (objectKey == null || objectKey.contains("..") || objectKey.contains("\\")) {
            throw new IllegalArgumentException("Invalid object key");
        }
        Path resolvedPath = rootPath.resolve(objectKey).normalize();
        if (!resolvedPath.startsWith(rootPath)) {
            throw new SecurityException("Path traversal attempt detected");
        }
        return resolvedPath;
    }

    @Override
    public void put(String objectKey, InputStream inputStream) throws IOException {
        Path targetPath = resolveKey(objectKey);
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream get(String objectKey) throws IOException {
        Path targetPath = resolveKey(objectKey);
        return Files.newInputStream(targetPath);
    }

    @Override
    public void delete(String objectKey) throws IOException {
        Path targetPath = resolveKey(objectKey);
        Files.deleteIfExists(targetPath);
    }

    @Override
    public boolean exists(String objectKey) {
        Path targetPath = resolveKey(objectKey);
        return Files.exists(targetPath);
    }
}
