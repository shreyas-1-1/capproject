package com.evaluation.repository;

import com.evaluation.model.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {
    List<EvaluationResult> findByAssignment_Student_StudentId(String studentId);
    EvaluationResult findByAssignment_AssignmentId(Long assignmentId);
}