package com.gsir.data.repository;

import com.gsir.data.model.DatasetPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetPolicyRepository extends JpaRepository<DatasetPolicyEntity, UUID> {
}

