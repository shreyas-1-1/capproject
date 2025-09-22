package com.evaluation.controller;

import com.evaluation.model.*;
import com.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/assignments/submit")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> submitAssignment(@RequestBody Map<String, String> request) {
        try {
            String studentId = request.get("studentId");
            String title = request.get("title");
            String codeContent = request.get("codeContent");
            String deadlineStr = request.get("deadline");

            // Parse deadline
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Assignment assignment = evaluationService.submitAssignment(studentId, title, codeContent, deadline);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Assignment submitted successfully",
                    "assignmentId", assignment.getAssignmentId(),
                    "evaluationResult", assignment.getEvaluationResult()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to submit assignment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/assignments/student/{studentId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Assignment>> getStudentAssignments(@PathVariable String studentId) {
        List<Assignment> assignments = evaluationService.getStudentAssignments(studentId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/results/student/{studentId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<EvaluationResult>> getStudentResults(@PathVariable String studentId) {
        List<EvaluationResult> results = evaluationService.getStudentResults(studentId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/assignments/{assignmentId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getAssignment(@PathVariable Long assignmentId) {
        Optional<Assignment> assignment = evaluationService.getAssignment(assignmentId);
        if (assignment.isPresent()) {
            return ResponseEntity.ok(assignment.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/results/assignment/{assignmentId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getEvaluationResult(@PathVariable Long assignmentId) {
        Optional<EvaluationResult> result = evaluationService.getEvaluationResult(assignmentId);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}