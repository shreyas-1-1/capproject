package com.evaluation.model;

public class TestCase {
    private String input;
    private String expectedOutput;
    private double weight;
    private String description;
    
    // Constructors
    public TestCase() {}
    
    public TestCase(String input, String expectedOutput, double weight, String description) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.weight = weight;
        this.description = description;
    }
    
    // Getters and Setters
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Business Methods
    public boolean validateOutput(String actualOutput) {
        return expectedOutput.trim().equals(actualOutput.trim());
    }
    
    public double calculatePartialScore(String actualOutput) {
        if (validateOutput(actualOutput)) {
            return weight;
        }
        // Could implement fuzzy matching for partial credit
        return 0.0;
    }
}