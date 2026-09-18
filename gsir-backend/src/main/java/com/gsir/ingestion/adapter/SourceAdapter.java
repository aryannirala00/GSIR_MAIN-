package com.gsir.ingestion.adapter;

import com.gsir.ingestion.model.NormalizedDocument;
import com.gsir.ingestion.model.SourceType;

import java.io.InputStream;
import java.util.UUID;

public interface SourceAdapter {
    SourceType getSupportedType();
    NormalizedDocument parse(InputStream content, UUID sourceId, UUID projectId);
}
