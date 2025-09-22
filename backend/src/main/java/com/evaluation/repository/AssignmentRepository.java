package com.evaluation.repository;

import com.evaluation.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByStudent_StudentId(String studentId);
    List<Assignment> findByTitle(String title);
}