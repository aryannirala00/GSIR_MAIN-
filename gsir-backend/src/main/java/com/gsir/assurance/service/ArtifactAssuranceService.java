package com.gsir.assurance.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.assurance.adapter.ResearchArtifactRepository;
import com.gsir.assurance.adapter.AssuranceAttestationRepository;
import com.gsir.assurance.model.ResearchArtifactEntity;
import com.gsir.assurance.model.AssuranceAttestationEntity;
import com.gsir.governance.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@Service
public class ArtifactAssuranceService {

    private final ResearchArtifactRepository artifactRepository;
    private final AssuranceAttestationRepository attestationRepository;
    private final UuidV7Generator idGenerator;
    private final AuditLogService auditLogService;

    public ArtifactAssuranceService(ResearchArtifactRepository artifactRepository,
                                    AssuranceAttestationRepository attestationRepository,
                                    UuidV7Generator idGenerator,
                                    AuditLogService auditLogService) {
        this.artifactRepository = artifactRepository;
        this.attestationRepository = attestationRepository;
        this.idGenerator = idGenerator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ResearchArtifactEntity registerArtifact(UUID projectId, UUID actorId, String objectType, String locationUri, String expectedHash, String hashAlgorithm) {
        ResearchArtifactEntity artifact = new ResearchArtifactEntity(
                idGenerator.generate(),
                projectId,
                objectType,
                locationUri,
                expectedHash,
                hashAlgorithm
        );
        artifact = artifactRepository.save(artifact);
        
        auditLogService.logEvent(projectId, actorId, "REGISTER_ARTIFACT", "ARTIFACT", artifact.getId().toString(), Map.of("objectType", objectType));
        return artifact;
    }

    @Transactional
    public boolean verifyArtifactHash(UUID projectId, UUID artifactId, UUID actorId, String actualHash) {
        Optional<ResearchArtifactEntity> artifactOpt = artifactRepository.findByIdAndProjectId(artifactId, projectId);
        if (artifactOpt.isEmpty()) {
            throw new IllegalArgumentException("Artifact not found or does not belong to the project");
        }
        
        ResearchArtifactEntity artifact = artifactOpt.get();
        boolean isValid = MessageDigest.isEqual(artifact.getExpectedHash().toLowerCase().getBytes(), actualHash.toLowerCase().getBytes());
        
        artifact.setStatus(isValid ? "VERIFIED" : "COMPROMISED");
        artifactRepository.save(artifact);
        
        auditLogService.logEvent(projectId, actorId, isValid ? "VERIFY_ARTIFACT_SUCCESS" : "VERIFY_ARTIFACT_FAILURE", "ARTIFACT", artifactId.toString(), Map.of("hash", actualHash));
        
        return isValid;
    }

    @Transactional
    public AssuranceAttestationEntity attestAssurance(UUID projectId, UUID artifactId, UUID actorId, String assuranceLevel, Double score, Map<String, Object> metadata) {
        if (score == null) {
            throw new IllegalArgumentException("Score must not be null");
        }
        com.gsir.common.math.NumericSafety.requireFinite(score);
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 1.0");
        }

        Optional<ResearchArtifactEntity> artifactOpt = artifactRepository.findByIdAndProjectId(artifactId, projectId);
        if (artifactOpt.isEmpty()) {
            throw new IllegalArgumentException("Artifact not found or does not belong to the project");
        }

        AssuranceAttestationEntity attestation = new AssuranceAttestationEntity(
                idGenerator.generate(),
                artifactId,
                projectId,
                assuranceLevel,
                score,
                metadata
        );
        attestation = attestationRepository.save(attestation);
        
        auditLogService.logEvent(projectId, actorId, "ATTEST_ARTIFACT", "ATTESTATION", attestation.getId().toString(), Map.of("level", assuranceLevel, "score", score));
        return attestation;
    }
}

