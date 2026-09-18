package com.gsir.execution.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.execution.adapter.ProjectSecretRepository;
import com.gsir.execution.model.ProjectSecretEntity;
import com.gsir.security.EncryptionService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class SecretManagementService {

    private final ProjectSecretRepository repository;
    private final EncryptionService encryptionService;
    private final UuidV7Generator idGenerator;

    public SecretManagementService(ProjectSecretRepository repository, EncryptionService encryptionService, UuidV7Generator idGenerator) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.idGenerator = idGenerator;
    }

    public void storeSecret(UUID projectId, String secretName, String plainTextValue) {
        String encrypted = encryptionService.encrypt(plainTextValue);
        
        Optional<ProjectSecretEntity> existing = repository.findByProjectIdAndSecretName(projectId, secretName);
        if (existing.isPresent()) {
            ProjectSecretEntity secret = existing.get();
            secret.setEncryptedValue(encrypted);
            repository.save(secret);
        } else {
            ProjectSecretEntity secret = new ProjectSecretEntity(idGenerator.generate(), projectId, secretName, encrypted);
            repository.save(secret);
        }
    }

    public String resolveSecret(UUID projectId, String secretName) {
        ProjectSecretEntity secret = repository.findByProjectIdAndSecretName(projectId, secretName)
                .orElseThrow(() -> new IllegalArgumentException("Secret not found: " + secretName));
                
        return encryptionService.decrypt(secret.getEncryptedValue());
    }
    
    public List<ProjectSecretEntity> listSecrets(UUID projectId) {
        return repository.findByProjectId(projectId);
    }
}
