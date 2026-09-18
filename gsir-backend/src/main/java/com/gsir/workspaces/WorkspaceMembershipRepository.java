package com.gsir.workspaces;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembershipEntity, WorkspaceMembershipId> {
}

