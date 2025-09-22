-- Initialize MySQL Database
CREATE DATABASE IF NOT EXISTS evaluation_db;
USE evaluation_db;

-- Sample data for testing
INSERT IGNORE INTO students (student_id, name, email) VALUES 
('STU001', 'John Doe', 'john.doe@example.com'),
('STU002', 'Jane Smith', 'jane.smith@example.com'),
('STU003', 'Alice Johnson', 'alice.johnson@example.com');