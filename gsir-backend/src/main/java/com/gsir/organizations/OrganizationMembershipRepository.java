package com.gsir.organizations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembershipEntity, OrganizationMembershipId> {
}

