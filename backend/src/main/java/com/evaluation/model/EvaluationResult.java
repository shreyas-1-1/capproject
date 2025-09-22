package com.evaluation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_results")
public class EvaluationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @Column(nullable = false)
    private double score;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String detailedFeedback;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @JsonBackReference("assignment-result")
    private Assignment assignment;

    // Constructors
    public EvaluationResult() {}

    public EvaluationResult(double score, String remarks, boolean passed) {
        this.score = score;
        this.remarks = remarks;
        this.passed = passed;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getDetailedFeedback() { return detailedFeedback; }
    public void setDetailedFeedback(String detailedFeedback) { this.detailedFeedback = detailedFeedback; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    // Business Methods
    public void generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Evaluation Report\n");
        report.append("Score: ").append(score).append("/100\n");
        report.append("Status: ").append(passed ? "PASSED" : "FAILED").append("\n");
        report.append("Remarks: ").append(remarks).append("\n");
        report.append("Timestamp: ").append(timestamp).append("\n");
        this.detailedFeedback = report.toString();
    }

    public String getGradeLetter() {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}