package com.gsir.files.storage;

import java.io.IOException;
import java.io.InputStream;

public interface ObjectStore {
    void put(String objectKey, InputStream inputStream) throws IOException;
    InputStream get(String objectKey) throws IOException;
    void delete(String objectKey) throws IOException;
    boolean exists(String objectKey);
}
