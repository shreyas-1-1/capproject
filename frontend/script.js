// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Global State
let currentStudent = null;
let currentAssignments = [];

// DOM Elements
const loginSection = document.getElementById('loginSection');
const submissionSection = document.getElementById('submissionSection');
const resultsSection = document.getElementById('resultsSection');
const loadingOverlay = document.getElementById('loadingOverlay');
const toast = document.getElementById('toast');

// Forms
const loginForm = document.getElementById('loginForm');
const assignmentForm = document.getElementById('assignmentForm');

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    setupExamples();
    
    // Set default deadline to 1 week from now
    const deadline = document.getElementById('deadline');
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    deadline.value = nextWeek.toISOString().slice(0, 16);
});

// Event Listeners Setup
function setupEventListeners() {
    // Login form
    loginForm.addEventListener('submit', handleLogin);
    
    // Assignment form
    assignmentForm.addEventListener('submit', handleAssignmentSubmission);
    
    // Assignment type change
    document.getElementById('assignmentType').addEventListener('change', handleAssignmentTypeChange);
    
    // Navigation
    setupNavigation();
}

// Handle student login
async function handleLogin(e) {
    e.preventDefault();
    
    const studentId = document.getElementById('studentId').value.trim();
    
    if (!studentId) {
        showToast('Please enter a valid Student ID', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        // Simulate login (in real app, you'd authenticate with backend)
        currentStudent = {
            studentId: studentId,
            name: `Student ${studentId}`,
            email: `${studentId}@example.com`
        };
        
        // Load student's assignment history
        await loadStudentData();
        
        // Switch to submission section
        showSection('submission');
        showToast('Login successful!', 'success');
        
    } catch (error) {
        console.error('Login error:', error);
        showToast('Login failed. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

// Handle assignment submission
async function handleAssignmentSubmission(e) {
    e.preventDefault();
    
    const formData = {
        studentId: currentStudent.studentId,
        title: document.getElementById('assignmentTitle').value,
        codeContent: document.getElementById('codeContent').value,
        deadline: document.getElementById('deadline').value
    };
    
    // Validate form data
    if (!validateAssignmentForm(formData)) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/assignments/submit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast('Assignment submitted and evaluated successfully!', 'success');
            
            // Clear form
            assignmentForm.reset();
            setDefaultDeadline();
            
            // Show results
            displayEvaluationResult(result.evaluationResult);
            showSection('results');
            
            // Refresh assignment history
            await loadStudentData();
            
        } else {
            throw new Error(result.message);
        }
        
    } catch (error) {
        console.error('Submission error:', error);
        showToast(`Submission failed: ${error.message}`, 'error');
    } finally {
        showLoading(false);
    }
}

// Load student data
async function loadStudentData() {
    try {
        const [assignmentsResponse, resultsResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/assignments/student/${currentStudent.studentId}`),
            fetch(`${API_BASE_URL}/results/student/${currentStudent.studentId}`)
        ]);
        
        if (assignmentsResponse.ok) {
            currentAssignments = await assignmentsResponse.json();
        }
        
        if (resultsResponse.ok) {
            const results = await resultsResponse.json();
            displayAssignmentHistory(results);
        }
        
    } catch (error) {
        console.error('Error loading student data:', error);
    }
}

// Display evaluation result
function displayEvaluationResult(result) {
    const resultContent = document.getElementById('resultContent');
    
    const resultCard = document.createElement('div');
    resultCard.className = `result-card ${result.passed ? '' : 'failed'} slide-up`;
    
    resultCard.innerHTML = `
        <div class="result-header">
            <h3>Latest Evaluation Result</h3>
            <div class="score ${result.passed ? 'passed' : 'failed'}">
                ${result.score.toFixed(1)}% - ${result.passed ? '✅ PASSED' : '❌ FAILED'}
            </div>
        </div>
        
        <div class="result-info">
            <p><strong>Grade:</strong> ${getGradeLetter(result.score)}</p>
            <p><strong>Evaluated:</strong> ${formatDateTime(result.timestamp)}</p>
            <p><strong>Remarks:</strong> ${result.remarks}</p>
        </div>
        
        <div class="result-details">
            <h4>Detailed Feedback:</h4>
            <pre>${result.detailedFeedback || 'No detailed feedback available'}</pre>
        </div>
    `;
    
    resultContent.innerHTML = '';
    resultContent.appendChild(resultCard);
}

// Display assignment history
function displayAssignmentHistory(results) {
    const historyContent = document.getElementById('historyContent');
    
    if (results.length === 0) {
        historyContent.innerHTML = '<p>No assignments submitted yet.</p>';
        return;
    }
    
    const historyHTML = results.map(result => `
        <div class="result-card ${result.passed ? '' : 'failed'}">
            <div class="result-header">
                <h4>Assignment #${result.assignment?.assignmentId || 'N/A'}</h4>
                <div class="score ${result.passed ? 'passed' : 'failed'}">
                    ${result.score.toFixed(1)}%
                </div>
            </div>
            <div class="result-info">
                <p><strong>Title:</strong> ${result.assignment?.title || 'N/A'}</p>
                <p><strong>Submitted:</strong> ${formatDateTime(result.assignment?.submissionTime)}</p>
                <p><strong>Status:</strong> ${result.passed ? 'Passed' : 'Failed'}</p>
            </div>
        </div>
    `).join('');
    
    historyContent.innerHTML = historyHTML;
}

// Setup code examples
function setupExamples() {
    const examples = document.querySelectorAll('.example');
    
    examples.forEach(example => {
        example.addEventListener('click', () => {
            const code = example.querySelector('code').textContent;
            const type = example.dataset.type;
            
            // Fill the form with example
            document.getElementById('codeContent').value = code;
            document.getElementById('assignmentType').value = type;
            
            // Set a relevant title
            const titles = {
                'hello-world': 'Hello World Program',
                'add-numbers': 'Add Two Numbers',
                'fibonacci': 'Fibonacci Sequence',
                'prime-check': 'Prime Number Check'
            };
            
            document.getElementById('assignmentTitle').value = titles[type] || 'Programming Assignment';
            
            showToast('Example code loaded!', 'info');
        });
    });
}

// Handle assignment type change
function handleAssignmentTypeChange(e) {
    const type = e.target.value;
    const codeContent = document.getElementById('codeContent');
    
    // Auto-fill title based on type
    const titles = {
        'hello-world': 'Hello World Program',
        'add-numbers': 'Add Two Numbers',
        'fibonacci': 'Fibonacci Sequence Generator',
        'prime-check': 'Prime Number Checker'
    };
    
    if (titles[type]) {
        document.getElementById('assignmentTitle').value = titles[type];
    }
    
    // Show relevant example
    const examples = document.querySelectorAll('.example');
    examples.forEach(example => {
        example.style.display = example.dataset.type === type ? 'block' : 'none';
    });
    
    if (type) {
        document.querySelector('.examples').scrollIntoView({ behavior: 'smooth' });
    }
}

// Setup navigation
function setupNavigation() {
    // Add navigation buttons dynamically
    const nav = document.createElement('div');
    nav.className = 'nav-buttons';
    nav.innerHTML = `
        <button class="nav-button" onclick="showSection('submission')">📝 Submit Assignment</button>
        <button class="nav-button" onclick="showSection('results')">📊 View Results</button>
        <button class="nav-button" onclick="logout()">🚪 Logout</button>
    `;
    
    // Insert navigation after login
    submissionSection.insertBefore(nav, submissionSection.firstChild);
    resultsSection.insertBefore(nav.cloneNode(true), resultsSection.firstChild);
}

// Section management
function showSection(section) {
    // Hide all sections
    loginSection.classList.add('hidden');
    submissionSection.classList.add('hidden');
    resultsSection.classList.add('hidden');
    
    // Show requested section
    switch (section) {
        case 'login':
            loginSection.classList.remove('hidden');
            break;
        case 'submission':
            if (currentStudent) {
                submissionSection.classList.remove('hidden');
                updateNavigation('submission');
            }
            break;
        case 'results':
            if (currentStudent) {
                resultsSection.classList.remove('hidden');
                updateNavigation('results');
            }
            break;
    }
}

// Update navigation active state
function updateNavigation(activeSection) {
    document.querySelectorAll('.nav-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // More reliable way to find active button
    document.querySelectorAll('.nav-button').forEach(button => {
        if ((activeSection === 'submission' && button.textContent.includes('Submit')) ||
            (activeSection === 'results' && button.textContent.includes('Results'))) {
            button.classList.add('active');
        }
    });
}

// Logout function
function logout() {
    currentStudent = null;
    currentAssignments = [];
    
    // Reset forms
    loginForm.reset();
    assignmentForm.reset();
    setDefaultDeadline();
    
    // Clear content
    document.getElementById('resultContent').innerHTML = '';
    document.getElementById('historyContent').innerHTML = '';
    
    // Show login section
    showSection('login');
    showToast('Logged out successfully', 'info');
}

// Utility Functions
function validateAssignmentForm(data) {
    if (!data.title.trim()) {
        showToast('Please enter an assignment title', 'error');
        return false;
    }
    
    if (!data.codeContent.trim()) {
        showToast('Please enter your code', 'error');
        return false;
    }
    
    if (!data.deadline) {
        showToast('Please set a deadline', 'error');
        return false;
    }
    
    const deadlineDate = new Date(data.deadline);
    if (deadlineDate <= new Date()) {
        showToast('Deadline must be in the future', 'error');
        return false;
    }
    
    return true;
}

function setDefaultDeadline() {
    const deadline = document.getElementById('deadline');
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    deadline.value = nextWeek.toISOString().slice(0, 16);
}

function showLoading(show) {
    if (show) {
        loadingOverlay.classList.remove('hidden');
    } else {
        loadingOverlay.classList.add('hidden');
    }
}

function showToast(message, type = 'info') {
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 5000);
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
}

function getGradeLetter(score) {
    if (score >= 90) return 'A';
    if (score >= 80) return 'B';
    if (score >= 70) return 'C';
    if (score >= 60) return 'D';
    return 'F';
}

// Error handling for network requests
async function handleApiError(response) {
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Network error' }));
        throw new Error(errorData.message || `HTTP ${response.status}`);
    }
    return response;
}

// Auto-save functionality
function setupAutoSave() {
    const codeContent = document.getElementById('codeContent');
    let saveTimeout;
    
    codeContent.addEventListener('input', () => {
        clearTimeout(saveTimeout);
        saveTimeout = setTimeout(() => {
            localStorage.setItem('draft-code', codeContent.value);
        }, 1000);
    });
    
    // Load draft on page load
    const draftCode = localStorage.getItem('draft-code');
    if (draftCode) {
        codeContent.value = draftCode;
    }
}

// Initialize auto-save
document.addEventListener('DOMContentLoaded', setupAutoSave);