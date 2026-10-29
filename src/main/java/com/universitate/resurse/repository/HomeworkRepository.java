package com.universitate.resurse.repository;

import com.universitate.resurse.model.Homework;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByCourseIdIn(List<Long> courseIds);
}