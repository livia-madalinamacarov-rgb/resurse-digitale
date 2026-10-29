package com.universitate.resurse.repository;

import com.universitate.resurse.model.SubscriptionCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionCredentialRepository
        extends JpaRepository<SubscriptionCredential, Long> {

    List<SubscriptionCredential> findByStudentId(Long studentId);
}