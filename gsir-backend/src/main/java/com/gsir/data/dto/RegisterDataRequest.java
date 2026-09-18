package com.gsir.data.dto;

import java.util.Map;

public class RegisterDataRequest {
    private String dataType;
    private String name;
    private String version;
    private String sourceReference;
    private String schemaReference;
    private String classification;
    private String storageReference;
    private Map<String, Object> metadata;

    public RegisterDataRequest() {}

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public String getSchemaReference() { return schemaReference; }
    public void setSchemaReference(String schemaReference) { this.schemaReference = schemaReference; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public String getStorageReference() { return storageReference; }
    public void setStorageReference(String storageReference) { this.storageReference = storageReference; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

