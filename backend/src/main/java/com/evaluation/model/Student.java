package com.evaluation.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {
    @Id
    private String studentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonManagedReference("student-assignments")
    private List<Assignment> assignments;

    // Constructors
    public Student() {}

    public Student(String studentId, String name, String email) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }

    // Business Methods
    public void submitAssignment(Assignment assignment) {
        assignment.setStudent(this);
        assignments.add(assignment);
    }

    public List<EvaluationResult> viewResults() {
        return assignments.stream()
                .map(Assignment::getEvaluationResult)
                .filter(result -> result != null)
                .collect(java.util.stream.Collectors.toList());
    }
}