package com.evaluation.model;

import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@Component
public class EvaluationEngine {
    private Map<String, List<TestCase>> testRepository;
    private ExecutorService executorService;
    private long timeoutMillis;
    private String sandboxConfig;
    
    public EvaluationEngine() {
        this.testRepository = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.timeoutMillis = 30000; // 30 seconds
        this.sandboxConfig = "default";
        initializeTestCases();
    }
    
    // Initialize predefined test cases
    private void initializeTestCases() {
        // Test cases for "Hello World" program
        List<TestCase> helloWorldTests = Arrays.asList(
            new TestCase("", "Hello World", 1.0, "Basic Hello World output"),
            new TestCase("", "Hello World\n", 1.0, "Hello World with newline")
        );
        testRepository.put("hello-world", helloWorldTests);
        
        // Test cases for "Add Two Numbers" program
        List<TestCase> addNumbersTests = Arrays.asList(
            new TestCase("5 3", "8", 1.0, "Add positive numbers"),
            new TestCase("10 20", "30", 1.0, "Add larger numbers"),
            new TestCase("0 0", "0", 1.0, "Add zeros"),
            new TestCase("-5 3", "-2", 1.0, "Add negative and positive")
        );
        testRepository.put("add-numbers", addNumbersTests);
        
        // Test cases for "Fibonacci" program
        List<TestCase> fibonacciTests = Arrays.asList(
            new TestCase("5", "0 1 1 2 3", 1.0, "First 5 Fibonacci numbers"),
            new TestCase("1", "0", 1.0, "First Fibonacci number"),
            new TestCase("8", "0 1 1 2 3 5 8 13", 1.0, "First 8 Fibonacci numbers")
        );
        testRepository.put("fibonacci", fibonacciTests);
        
        // Test cases for "Prime Check" program
        List<TestCase> primeTests = Arrays.asList(
            new TestCase("7", "Prime", 1.0, "Check prime number"),
            new TestCase("4", "Not Prime", 1.0, "Check composite number"),
            new TestCase("2", "Prime", 1.0, "Check smallest prime"),
            new TestCase("1", "Not Prime", 1.0, "Check number 1")
        );
        testRepository.put("prime-check", primeTests);
    }
    
    public EvaluationResult evaluateAssignment(Assignment assignment) {
        try {
            String assignmentType = determineAssignmentType(assignment);
            List<TestCase> testCases = getTestCases(assignmentType);
            
            if (testCases.isEmpty()) {
                return new EvaluationResult(0.0, "No test cases found for this assignment type", false);
            }
            
            List<TestResult> results = runTestCases(assignment, testCases);
            return generateEvaluationResult(assignment, results);
            
        } catch (Exception e) {
            return new EvaluationResult(0.0, "Evaluation failed: " + e.getMessage(), false);
        }
    }
    
    private String determineAssignmentType(Assignment assignment) {
        String title = assignment.getTitle().toLowerCase();
        String code = assignment.getCodeContent().toLowerCase();
        
        if (title.contains("hello") || code.contains("hello world")) {
            return "hello-world";
        } else if (title.contains("add") || title.contains("sum") || code.contains("add")) {
            return "add-numbers";
        } else if (title.contains("fibonacci") || code.contains("fibonacci")) {
            return "fibonacci";
        } else if (title.contains("prime") || code.contains("prime")) {
            return "prime-check";
        }
        
        return "hello-world"; // Default fallback
    }
    
    private List<TestCase> getTestCases(String assignmentType) {
        return testRepository.getOrDefault(assignmentType, new ArrayList<>());
    }
    
    private List<TestResult> runTestCases(Assignment assignment, List<TestCase> testCases) {
        List<TestResult> results = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            try {
                String output = executeCode(assignment.getCodeContent(), testCase.getInput());
                boolean passed = testCase.validateOutput(output);
                double score = passed ? testCase.getWeight() : 0.0;
                
                results.add(new TestResult(testCase, output, passed, score));
            } catch (Exception e) {
                results.add(new TestResult(testCase, "Error: " + e.getMessage(), false, 0.0));
            }
        }
        
        return results;
    }
    
    private String executeCode(String code, String input) throws Exception {
        // Simple Java code execution simulation
        // In production, this would use a proper sandboxed execution environment
        
        if (code.contains("System.out.println(\"Hello World\")") && input.isEmpty()) {
            return "Hello World";
        }
        
        if (code.contains("Scanner") && code.contains("nextInt()") && code.contains("+")) {
            String[] inputs = input.split(" ");
            if (inputs.length == 2) {
                int a = Integer.parseInt(inputs[0]);
                int b = Integer.parseInt(inputs[1]);
                return String.valueOf(a + b);
            }
        }
        
        if (code.contains("fibonacci") || code.contains("Fibonacci")) {
            int n = Integer.parseInt(input);
            return generateFibonacci(n);
        }
        
        if (code.contains("prime") || code.contains("Prime")) {
            int num = Integer.parseInt(input);
            return isPrime(num) ? "Prime" : "Not Prime";
        }
        
        return "No output"; // Default fallback
    }
    
    private String generateFibonacci(int n) {
        if (n <= 0) return "";
        if (n == 1) return "0";
        
        StringBuilder result = new StringBuilder();
        int a = 0, b = 1;
        result.append(a);
        
        for (int i = 1; i < n; i++) {
            result.append(" ").append(b);
            int temp = a + b;
            a = b;
            b = temp;
        }
        
        return result.toString();
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
    
    private EvaluationResult generateEvaluationResult(Assignment assignment, List<TestResult> results) {
        double totalScore = 0.0;
        double maxScore = 0.0;
        StringBuilder feedback = new StringBuilder();
        
        int passedTests = 0;
        for (TestResult result : results) {
            totalScore += result.getScore();
            maxScore += result.getTestCase().getWeight();
            if (result.isPassed()) passedTests++;
            
            feedback.append(String.format("Test: %s - %s (Score: %.1f/%.1f)\n", 
                result.getTestCase().getDescription(),
                result.isPassed() ? "PASSED" : "FAILED",
                result.getScore(),
                result.getTestCase().getWeight()));
        }
        
        double percentage = maxScore > 0 ? (totalScore / maxScore) * 100 : 0;
        boolean passed = percentage >= 60; // 60% passing threshold
        
        String remarks = String.format("Passed %d out of %d test cases. Score: %.2f%%", 
            passedTests, results.size(), percentage);
        
        EvaluationResult evaluationResult = new EvaluationResult(percentage, remarks, passed);
        evaluationResult.setDetailedFeedback(feedback.toString());
        evaluationResult.setAssignment(assignment);
        
        return evaluationResult;
    }
    
    // Helper class for test results
    private static class TestResult {
        private TestCase testCase;
        private String actualOutput;
        private boolean passed;
        private double score;
        
        public TestResult(TestCase testCase, String actualOutput, boolean passed, double score) {
            this.testCase = testCase;
            this.actualOutput = actualOutput;
            this.passed = passed;
            this.score = score;
        }
        
        // Getters
        public TestCase getTestCase() { return testCase; }
        public String getActualOutput() { return actualOutput; }
        public boolean isPassed() { return passed; }
        public double getScore() { return score; }
    }
    
    // Getters and Setters
    public Map<String, List<TestCase>> getTestRepository() { return testRepository; }
    public void setTestRepository(Map<String, List<TestCase>> testRepository) { this.testRepository = testRepository; }
    
    public long getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(long timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    
    public String getSandboxConfig() { return sandboxConfig; }
    public void setSandboxConfig(String sandboxConfig) { this.sandboxConfig = sandboxConfig; }
}