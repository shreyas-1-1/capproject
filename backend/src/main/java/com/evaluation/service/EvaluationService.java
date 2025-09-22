package com.evaluation.service;

import com.evaluation.model.*;
import com.evaluation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private EvaluationResultRepository evaluationResultRepository;
    
    @Autowired
    private EvaluationEngine evaluationEngine;
    
    @Autowired
    private LoggingService loggingService;
    
    public Assignment submitAssignment(String studentId, String title, String codeContent, LocalDateTime deadline) {
        try {
            // Find or create student
            Student student = studentRepository.findById(studentId)
                .orElse(new Student(studentId, "Student " + studentId, studentId + "@example.com"));
            
            if (student.getName().startsWith("Student ")) {
                studentRepository.save(student);
            }
            
            // Create assignment
            Assignment assignment = new Assignment(title, "/uploads/" + System.currentTimeMillis() + ".java", deadline, codeContent);
            assignment.setStudent(student);
            
            // Save assignment
            assignment = assignmentRepository.save(assignment);
            
            // Evaluate assignment
            EvaluationResult result = evaluationEngine.evaluateAssignment(assignment);
            result.setAssignment(assignment);
            result.generateReport();
            
            // Save evaluation result
            evaluationResultRepository.save(result);
            assignment.setEvaluationResult(result);
            
            loggingService.logInfo("Assignment submitted and evaluated successfully", 
                "StudentId: " + studentId + ", AssignmentId: " + assignment.getAssignmentId());
            
            return assignment;
            
        } catch (Exception e) {
            loggingService.logError("Failed to submit assignment", e.getMessage(), 
                "StudentId: " + studentId + ", Title: " + title);
            throw new RuntimeException("Failed to submit assignment: " + e.getMessage());
        }
    }
    
    public List<Assignment> getStudentAssignments(String studentId) {
        return assignmentRepository.findByStudent_StudentId(studentId);
    }
    
    public List<EvaluationResult> getStudentResults(String studentId) {
        return evaluationResultRepository.findByAssignment_Student_StudentId(studentId);
    }
    
    public Optional<Assignment> getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId);
    }
    
    public Optional<EvaluationResult> getEvaluationResult(Long assignmentId) {
        return Optional.ofNullable(evaluationResultRepository.findByAssignment_AssignmentId(assignmentId));
    }
}