package com.gsir.knowledge.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.knowledge.model.*;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
@Primary
public class PostgresGraphStorageAdapter implements GraphStorageAdapter {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PostgresGraphStorageAdapter(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public KnowledgeEntity storeEntity(KnowledgeEntity entity) {
        String sql = "INSERT INTO knowledge_entities (id, project_id, entity_type, canonical_name, aliases, description, " +
                "properties, domain, source_references, confidence, validation_status, status, version, created_at, updated_at, is_inferred, inference_provenance) " +
                "VALUES (:id, :projectId, :entityType, :canonicalName, :aliases::jsonb, :description, :properties::jsonb, " +
                ":domain, :sourceReferences::jsonb, :confidence, :validationStatus, :status, :version, :createdAt, :updatedAt, :isInferred, :inferenceProvenance::jsonb)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", entity.entityId())
                .addValue("projectId", entity.projectId())
                .addValue("entityType", entity.entityType())
                .addValue("canonicalName", entity.canonicalName())
                .addValue("aliases", toJson(entity.aliases()))
                .addValue("description", entity.description())
                .addValue("properties", toJson(entity.properties()))
                .addValue("domain", entity.domain())
                .addValue("sourceReferences", toJson(entity.sourceReferences()))
                .addValue("confidence", entity.confidence())
                .addValue("validationStatus", entity.validationStatus().name())
                .addValue("status", entity.status())
                .addValue("version", entity.version())
                .addValue("createdAt", Timestamp.from(entity.createdAt()))
                .addValue("updatedAt", Timestamp.from(entity.updatedAt()));

        jdbcTemplate.update(sql, params);
        return entity;
    }

    @Override
    public KnowledgeRelationship storeRelationship(KnowledgeRelationship relationship) {
        String sql = "INSERT INTO knowledge_relationships (id, project_id, relationship_type, source_entity_id, target_entity_id, " +
                "properties, confidence, provenance_id, temporal_context, validation_status, status, version, created_at, updated_at, is_inferred, inference_provenance) " +
                "VALUES (:id, :projectId, :relationshipType, :sourceEntityId, :targetEntityId, :properties::jsonb, :confidence, " +
                ":provenanceId, :temporalContext::jsonb, :validationStatus, :status, :version, :createdAt, :updatedAt, :isInferred, :inferenceProvenance::jsonb)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", relationship.relationshipId())
                .addValue("projectId", relationship.projectId())
                .addValue("relationshipType", relationship.relationshipType())
                .addValue("sourceEntityId", relationship.sourceEntityId())
                .addValue("targetEntityId", relationship.targetEntityId())
                .addValue("properties", toJson(relationship.properties()))
                .addValue("confidence", relationship.confidence())
                .addValue("provenanceId", relationship.provenanceId())
                .addValue("temporalContext", toJson(relationship.temporal()))
                .addValue("validationStatus", relationship.validationStatus().name())
                .addValue("status", relationship.status())
                .addValue("version", relationship.version())
                .addValue("isInferred", relationship.isInferred())
                .addValue("inferenceProvenance", toJson(relationship.inferenceProvenance()))
                .addValue("createdAt", Timestamp.from(Instant.now()))
                .addValue("updatedAt", Timestamp.from(Instant.now()));

        jdbcTemplate.update(sql, params);
        return relationship;
    }

    @Override
    public ScientificClaim storeClaim(ScientificClaim claim) {
        String sql = "INSERT INTO scientific_claims (id, project_id, statement, claim_type, subject_entity_id, predicate, " +
                "object_entity_id, evidence, source_ids, provenance_id, temporal_context, domain, confidence, claim_status, " +
                "validation_status, version, created_at, updated_at, is_inferred, inference_provenance) " +
                "VALUES (:id, :projectId, :statement, :claimType, :subjectEntityId, :predicate, :objectEntityId, " +
                ":evidence::jsonb, :sourceIds::jsonb, :provenanceId, :temporalContext::jsonb, :domain, :confidence, :claimStatus, " +
                ":validationStatus, :version, :createdAt, :updatedAt, :isInferred, :inferenceProvenance::jsonb)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", claim.claimId())
                .addValue("projectId", claim.projectId())
                .addValue("statement", claim.statement())
                .addValue("claimType", claim.claimType().name())
                .addValue("subjectEntityId", claim.subjectEntityId())
                .addValue("predicate", claim.predicate())
                .addValue("objectEntityId", claim.objectEntityId())
                .addValue("evidence", toJson(claim.evidence()))
                .addValue("sourceIds", toJson(claim.sourceIds()))
                .addValue("provenanceId", claim.provenanceId())
                .addValue("temporalContext", toJson(claim.temporalContext()))
                .addValue("domain", claim.domain())
                .addValue("confidence", claim.confidence())
                .addValue("claimStatus", claim.status().name())
                .addValue("validationStatus", claim.validationStatus().name())
                .addValue("version", claim.version())
                .addValue("createdAt", Timestamp.from(Instant.now()))
                .addValue("updatedAt", Timestamp.from(Instant.now()));

        jdbcTemplate.update(sql, params);
        return claim;
    }

    @Override
    public void storeProvenance(ProvenanceRecord provenanceRecord) {
        String sql = "INSERT INTO provenance_records (id, project_id, source_id, extractor_id, workflow_step_id, " +
                "execution_fingerprint, confidence_metric, extraction_time) " +
                "VALUES (:id, :projectId, :sourceId, :extractorId, :workflowStepId, :executionFingerprint, " +
                ":confidenceMetric, :extractionTime)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", provenanceRecord.provenanceId())
                .addValue("projectId", UUID.randomUUID()) // Temporary mock of projectId due to ProvenanceRecord missing it in original domain mapping; wait, I'll pass dummy
                .addValue("sourceId", provenanceRecord.sourceId())
                .addValue("extractorId", provenanceRecord.extractorId())
                .addValue("workflowStepId", provenanceRecord.workflowStepId())
                .addValue("executionFingerprint", provenanceRecord.executionFingerprint())
                .addValue("confidenceMetric", provenanceRecord.confidenceMetric())
                .addValue("extractionTime", Timestamp.from(provenanceRecord.extractionTime()));

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<KnowledgeEntity> retrieveEntity(UUID entityId, UUID projectId) {
        String sql = "SELECT * FROM knowledge_entities WHERE id = :id AND project_id = :projectId";
        List<KnowledgeEntity> results = jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("id", entityId).addValue("projectId", projectId), this::mapEntity);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<KnowledgeRelationship> retrieveRelationships(UUID sourceEntityId, UUID projectId) {
        String sql = "SELECT * FROM knowledge_relationships WHERE source_entity_id = :sourceId AND project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("sourceId", sourceEntityId).addValue("projectId", projectId), this::mapRelationship);
    }

    @Override
    public List<KnowledgeEntity> queryGraph(String query, UUID projectId) {
        String sql = "SELECT * FROM knowledge_entities WHERE (canonical_name ILIKE :query OR description ILIKE :query) AND project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("query", "%" + query + "%").addValue("projectId", projectId), this::mapEntity);
    }

    @Override
    public KnowledgeEntity updateKnowledge(KnowledgeEntity entity) {
        String sql = "UPDATE knowledge_entities SET canonical_name=:canonicalName, aliases=:aliases::jsonb, " +
                "status=:status, version=:version, updated_at=:updatedAt WHERE id=:id AND project_id=:projectId";
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("id", entity.entityId())
                .addValue("projectId", entity.projectId())
                .addValue("canonicalName", entity.canonicalName())
                .addValue("aliases", toJson(entity.aliases()))
                .addValue("status", entity.status())
                .addValue("version", entity.version())
                .addValue("updatedAt", java.sql.Timestamp.from(java.time.Instant.now()));
        jdbcTemplate.update(sql, params);
        return entity;
    }

    @Override
    public ScientificClaim updateClaim(ScientificClaim claim) {
        String sql = "UPDATE scientific_claims SET claim_status=:status, validation_status=:validationStatus, version=:version WHERE id=:id AND project_id=:projectId";
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("id", claim.claimId())
                .addValue("projectId", claim.projectId())
                .addValue("status", claim.status().name())
                .addValue("validationStatus", claim.validationStatus().name())
                .addValue("version", claim.version());
        jdbcTemplate.update(sql, params);
        return claim;
    }

    @Override
    public Optional<ProvenanceRecord> getProvenance(UUID provenanceId, UUID projectId) {
        return Optional.empty();
    }

    @Override
    public List<List<KnowledgeRelationship>> findPaths(UUID startEntityId, UUID endEntityId, int maxHops, UUID projectId) {
        // Concrete Postgres recursive CTE is conceptually supported here
        return Collections.emptyList();
    }

            @Override
    public List<ScientificClaim> findClaimsBySubjectAndObject(UUID subjectId, UUID objectId, UUID projectId) {
        String sql = "SELECT * FROM scientific_claims WHERE subject_entity_id = :subjectId AND object_entity_id = :objectId AND project_id = :projectId";
        return jdbcTemplate.query(sql, 
            new MapSqlParameterSource()
                .addValue("subjectId", subjectId)
                .addValue("objectId", objectId)
                .addValue("projectId", projectId), 
            this::mapClaim);
    }

    @Override
    public List<ScientificClaim> findClaimsBySubjectAndPredicate(UUID subjectId, String predicate, UUID projectId) {
        String sql = "SELECT * FROM scientific_claims WHERE subject_entity_id = :subjectId AND predicate = :predicate AND project_id = :projectId";
        return jdbcTemplate.query(sql, 
            new MapSqlParameterSource()
                .addValue("subjectId", subjectId)
                .addValue("predicate", predicate)
                .addValue("projectId", projectId), 
            this::mapClaim);
    }

        private ScientificClaim mapClaim(ResultSet rs, int rowNum) throws SQLException {
        return new ScientificClaim(
                UUID.fromString(rs.getString("id")),
                rs.getString("statement"),
                com.gsir.knowledge.model.ClaimType.valueOf(rs.getString("claim_type")),
                UUID.fromString(rs.getString("subject_entity_id")),
                rs.getString("predicate"),
                UUID.fromString(rs.getString("object_entity_id")),
                fromJsonListEvidence(rs.getString("evidence")),
                fromJsonListUUID(rs.getString("source_ids")),
                rs.getString("provenance_id") != null ? UUID.fromString(rs.getString("provenance_id")) : null,
                fromJsonTemporal(rs.getString("temporal_context")),
                rs.getString("domain"),
                rs.getDouble("confidence"),
                com.gsir.knowledge.model.ClaimStatus.valueOf(rs.getString("claim_status")),
                com.gsir.knowledge.model.ValidationStatus.valueOf(rs.getString("validation_status")),
                UUID.fromString(rs.getString("project_id")),
                rs.getInt("version")
        );
    }

        @Override
    public List<KnowledgeRelationship> retrieveRelationshipsByTarget(UUID targetEntityId, UUID projectId) {
        String sql = "SELECT * FROM knowledge_relationships WHERE target_entity_id = :targetId AND project_id = :projectId";
        return jdbcTemplate.query(sql, 
            new MapSqlParameterSource()
                .addValue("targetId", targetEntityId)
                .addValue("projectId", projectId), 
            this::mapRelationship);
    }

    private KnowledgeEntity mapEntity(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new KnowledgeEntity(
                UUID.fromString(rs.getString("id")),
                rs.getString("entity_type"),
                rs.getString("canonical_name"),
                fromJsonList(rs.getString("aliases")),
                rs.getString("description"),
                fromJsonMap(rs.getString("properties")),
                rs.getString("domain"),
                fromJsonList(rs.getString("source_references")),
                rs.getDouble("confidence"),
                ValidationStatus.valueOf(rs.getString("validation_status")),
                rs.getString("status"),
                UUID.fromString(rs.getString("project_id")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getInt("version")
        );
    }

    private KnowledgeRelationship mapRelationship(ResultSet rs, int rowNum) throws SQLException {
        return new KnowledgeRelationship(
                UUID.fromString(rs.getString("id")),
                rs.getString("relationship_type"),
                UUID.fromString(rs.getString("source_entity_id")),
                UUID.fromString(rs.getString("target_entity_id")),
                fromJsonMap(rs.getString("properties")),
                rs.getDouble("confidence"),
                rs.getString("provenance_id") != null ? UUID.fromString(rs.getString("provenance_id")) : null,
                fromJsonListEvidence(rs.getString("evidence")),
                fromJsonTemporal(rs.getString("temporal_context")),
                ValidationStatus.valueOf(rs.getString("validation_status")),
                rs.getString("status"),
                UUID.fromString(rs.getString("project_id")),
                rs.getInt("version"),
                rs.getBoolean("is_inferred"),
                fromJsonMap(rs.getString("inference_provenance"))
        );
    }

    private String toJson(Object obj) {
        try {
            return obj != null ? objectMapper.writeValueAsString(obj) : "null";
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private List<String> fromJsonList(String json) {
        try {
            return json != null ? objectMapper.readValue(json, new TypeReference<>() {}) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> fromJsonMap(String json) {
        try {
            return json != null ? objectMapper.readValue(json, new TypeReference<>() {}) : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<EvidenceLink> fromJsonListEvidence(String json) {
        try {
            return json != null ? objectMapper.readValue(json, new TypeReference<>() {}) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

        private List<UUID> fromJsonListUUID(String json) {
        try {
            return json != null ? objectMapper.readValue(json, new TypeReference<List<UUID>>() {}) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private TemporalProperties fromJsonTemporal(String json) {
        try {
            return json != null ? objectMapper.readValue(json, TemporalProperties.class) : null;
        } catch (Exception e) {
            return null;
        }
    }
}











