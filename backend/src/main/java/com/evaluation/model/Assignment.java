package com.evaluation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String submissionFilePath;

    @Column(nullable = false)
    private LocalDateTime submissionTime;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(columnDefinition = "TEXT")
    private String codeContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonBackReference("student-assignments")
    private Student student;

    @OneToOne(mappedBy = "assignment", cascade = CascadeType.ALL)
    @JsonManagedReference("assignment-result")
    private EvaluationResult evaluationResult;

    // Constructors
    public Assignment() {}

    public Assignment(String title, String submissionFilePath, LocalDateTime deadline, String codeContent) {
        this.title = title;
        this.submissionFilePath = submissionFilePath;
        this.submissionTime = LocalDateTime.now();
        this.deadline = deadline;
        this.codeContent = codeContent;
    }

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubmissionFilePath() { return submissionFilePath; }
    public void setSubmissionFilePath(String submissionFilePath) { this.submissionFilePath = submissionFilePath; }

    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(LocalDateTime submissionTime) { this.submissionTime = submissionTime; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public EvaluationResult getEvaluationResult() { return evaluationResult; }
    public void setEvaluationResult(EvaluationResult evaluationResult) { this.evaluationResult = evaluationResult; }

    // Business Methods
    public boolean isLate() {
        return submissionTime.isAfter(deadline);
    }

    public String getFileReference() {
        return submissionFilePath;
    }

    public void updateMetadata(String title, LocalDateTime deadline) {
        this.title = title;
        this.deadline = deadline;
    }
}