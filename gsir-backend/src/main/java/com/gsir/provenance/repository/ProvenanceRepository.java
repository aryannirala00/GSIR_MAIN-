package com.gsir.provenance.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.provenance.model.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ProvenanceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ProvenanceRepository(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public ProvAgent saveAgent(ProvAgent agent) {
        String sql = "INSERT INTO prov_agents (id, project_id, agent_type, name, metadata, created_at) " +
                "VALUES (:id, :projectId, :agentType, :name, :metadata::jsonb, :createdAt)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", agent.id())
                .addValue("projectId", agent.projectId())
                .addValue("agentType", agent.agentType().name())
                .addValue("name", agent.name())
                .addValue("metadata", toJson(agent.metadata()))
                .addValue("createdAt", Timestamp.from(agent.createdAt())));
        return agent;
    }

    public ProvActivity saveActivity(ProvActivity activity) {
        String sql = "INSERT INTO prov_activities (id, project_id, activity_type, status, start_time, end_time, parameters, execution_environment, created_at) " +
                "VALUES (:id, :projectId, :activityType, :status, :startTime, :endTime, :parameters::jsonb, :executionEnvironment::jsonb, :createdAt)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", activity.id())
                .addValue("projectId", activity.projectId())
                .addValue("activityType", activity.activityType())
                .addValue("status", activity.status())
                .addValue("startTime", Timestamp.from(activity.startTime()))
                .addValue("endTime", activity.endTime() != null ? Timestamp.from(activity.endTime()) : null)
                .addValue("parameters", toJson(activity.parameters()))
                .addValue("executionEnvironment", toJson(activity.executionEnvironment()))
                .addValue("createdAt", Timestamp.from(activity.createdAt())));
        return activity;
    }

    public ProvArtifact saveArtifact(ProvArtifact artifact) {
        String sql = "INSERT INTO prov_artifacts (id, project_id, artifact_type, content_hash, version_number, location_uri, metadata, created_at) " +
                "VALUES (:id, :projectId, :artifactType, :contentHash, :versionNumber, :locationUri, :metadata::jsonb, :createdAt)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", artifact.id())
                .addValue("projectId", artifact.projectId())
                .addValue("artifactType", artifact.artifactType())
                .addValue("contentHash", artifact.contentHash())
                .addValue("versionNumber", artifact.versionNumber())
                .addValue("locationUri", artifact.locationUri())
                .addValue("metadata", toJson(artifact.metadata()))
                .addValue("createdAt", Timestamp.from(artifact.createdAt())));
        return artifact;
    }

    public ProvRelationship saveRelationship(ProvRelationship rel) {
        String sql = "INSERT INTO prov_relationships (id, project_id, relationship_type, source_id, target_id, created_at) " +
                "VALUES (:id, :projectId, :relationshipType, :sourceId, :targetId, :createdAt)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", rel.id())
                .addValue("projectId", rel.projectId())
                .addValue("relationshipType", rel.relationshipType().name())
                .addValue("sourceId", rel.sourceId())
                .addValue("targetId", rel.targetId())
                .addValue("createdAt", Timestamp.from(rel.createdAt())));
        return rel;
    }

    public List<ProvRelationship> findForwardLineage(UUID sourceId, UUID projectId) {
        String sql = "SELECT * FROM prov_relationships WHERE source_id = :sourceId AND project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("sourceId", sourceId).addValue("projectId", projectId), this::mapRelationship);
    }

    public List<ProvRelationship> findBackwardLineage(UUID targetId, UUID projectId) {
        String sql = "SELECT * FROM prov_relationships WHERE target_id = :targetId AND project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("targetId", targetId).addValue("projectId", projectId), this::mapRelationship);
    }

    private ProvRelationship mapRelationship(ResultSet rs, int rowNum) throws SQLException {
        return new ProvRelationship(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                RelationshipType.valueOf(rs.getString("relationship_type")),
                UUID.fromString(rs.getString("source_id")),
                UUID.fromString(rs.getString("target_id")),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    public List<ProvRelationship> findRelationshipsByProjectId(UUID projectId) {
        String sql = "SELECT * FROM prov_relationships WHERE project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("projectId", projectId), this::mapRelationship);
    }

    public List<ProvArtifact> findArtifactsByProjectId(UUID projectId) {
        String sql = "SELECT * FROM prov_artifacts WHERE project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("projectId", projectId), (rs, rowNum) -> new ProvArtifact(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                rs.getString("artifact_type"),
                rs.getString("content_hash"),
                rs.getInt("version_number"),
                rs.getString("location_uri"),
                fromJson(rs.getString("metadata")),
                rs.getTimestamp("created_at").toInstant()
        ));
    }

    public List<ProvActivity> findActivitiesByProjectId(UUID projectId) {
        String sql = "SELECT * FROM prov_activities WHERE project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("projectId", projectId), (rs, rowNum) -> new ProvActivity(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                rs.getString("activity_type"),
                rs.getString("status"),
                rs.getTimestamp("start_time").toInstant(),
                rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toInstant() : null,
                fromJson(rs.getString("parameters")),
                fromJson(rs.getString("execution_environment")),
                rs.getTimestamp("created_at").toInstant()
        ));
    }

    public List<ProvAgent> findAgentsByProjectId(UUID projectId) {
        String sql = "SELECT * FROM prov_agents WHERE project_id = :projectId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("projectId", projectId), (rs, rowNum) -> new ProvAgent(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                AgentType.valueOf(rs.getString("agent_type")),
                rs.getString("name"),
                fromJson(rs.getString("metadata")),
                rs.getTimestamp("created_at").toInstant()
        ));
    }

    public boolean artifactExists(UUID id, UUID projectId) {
        String sql = "SELECT COUNT(*) FROM prov_artifacts WHERE id = :id AND project_id = :projectId";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("id", id).addValue("projectId", projectId), Integer.class);
        return count != null && count > 0;
    }

    public boolean activityExists(UUID id, UUID projectId) {
        String sql = "SELECT COUNT(*) FROM prov_activities WHERE id = :id AND project_id = :projectId";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("id", id).addValue("projectId", projectId), Integer.class);
        return count != null && count > 0;
    }

    public boolean agentExists(UUID id, UUID projectId) {
        String sql = "SELECT COUNT(*) FROM prov_agents WHERE id = :id AND project_id = :projectId";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("id", id).addValue("projectId", projectId), Integer.class);
        return count != null && count > 0;
    }

    private String toJson(Object obj) {
        try {
            return obj != null ? objectMapper.writeValueAsString(obj) : "{}";
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return json != null ? objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {}) : Collections.emptyMap();
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}
