package com.gsir.knowledge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ontologies")
public class OntologyEntity {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "namespace", nullable = false)
    private String namespace;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "domain")
    private String domain;

    @Column(name = "source_uri")
    private String sourceUri;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OntologyEntity() {}

    public OntologyEntity(UUID id, UUID projectId, String name, String namespace, String version, String domain, String sourceUri) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.namespace = namespace;
        this.version = version;
        this.domain = domain;
        this.sourceUri = sourceUri;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public String getVersion() { return version; }
    public String getDomain() { return domain; }
    public String getSourceUri() { return sourceUri; }
    public Instant getCreatedAt() { return createdAt; }
}

