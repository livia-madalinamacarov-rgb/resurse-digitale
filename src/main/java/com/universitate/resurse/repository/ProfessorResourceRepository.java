package com.universitate.resurse.repository;

import com.universitate.resurse.model.ProfessorResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessorResourceRepository
        extends JpaRepository<ProfessorResource, Long> {

    Optional<ProfessorResource> findByProfessorId(Long professorId);
}