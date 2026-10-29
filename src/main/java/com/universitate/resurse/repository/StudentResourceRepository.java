package com.universitate.resurse.repository;
import java.util.List;
import com.universitate.resurse.model.StudentResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentResourceRepository extends JpaRepository<StudentResource, Long> {
    Optional<StudentResource> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<StudentResource> findByStudentId(Long studentId);
    List<StudentResource> findByCourseId(Long courseId);

}