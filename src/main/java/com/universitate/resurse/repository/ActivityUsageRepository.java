package com.universitate.resurse.repository;

import com.universitate.resurse.model.ActivityUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityUsageRepository extends JpaRepository<ActivityUsage, Long> {
    List<ActivityUsage> findByStudentId(Long studentId);
    List<ActivityUsage> findByCourseId(Long courseId);
}