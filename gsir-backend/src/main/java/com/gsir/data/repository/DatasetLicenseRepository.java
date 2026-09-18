package com.gsir.data.repository;

import com.gsir.data.model.DatasetLicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatasetLicenseRepository extends JpaRepository<DatasetLicenseEntity, UUID> {
}

