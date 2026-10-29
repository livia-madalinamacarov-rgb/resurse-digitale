package com.universitate.resurse.repository;

import com.universitate.resurse.model.ProfessorRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRequestRepository
        extends JpaRepository<ProfessorRequest, Long> {
}