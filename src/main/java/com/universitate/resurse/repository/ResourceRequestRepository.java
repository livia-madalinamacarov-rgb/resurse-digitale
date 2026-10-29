package com.universitate.resurse.repository;

import com.universitate.resurse.model.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {

    List<ResourceRequest> findByApprovedFalse();
    List<ResourceRequest> findByNeedsAdminApprovalTrueAndApprovedFalse();

}