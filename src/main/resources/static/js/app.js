// Global App State
const state = {
    token: localStorage.getItem('token') || null,
    user: null, // { username, role }
    studentProfile: null,
    jobs: [],
    applications: [],
    students: []
};

// UI Section Elements
const el = {
    authSection: document.getElementById('auth-section'),
    appSection: document.getElementById('app-section'),
    loginForm: document.getElementById('login-form'),
    registerForm: document.getElementById('register-form'),
    toggleToRegister: document.getElementById('toggle-to-register'),
    toggleToLogin: document.getElementById('toggle-to-login'),
    navUserInfo: document.getElementById('nav-user-info'),
    logoutBtn: document.getElementById('logout-btn'),
    toast: document.getElementById('toast'),
    
    // Dashboards
    studentDash: document.getElementById('student-dashboard'),
    recruiterDash: document.getElementById('recruiter-dashboard'),
    adminDash: document.getElementById('admin-dashboard'),
    
    // Student Elements
    profileForm: document.getElementById('profile-form'),
    profileEmail: document.getElementById('profile-email'),
    profilePhone: document.getElementById('profile-phone'),
    profileBranch: document.getElementById('profile-branch'),
    profileYear: document.getElementById('profile-year'),
    profileCollege: document.getElementById('profile-college'),
    profileSkills: document.getElementById('profile-skills'),
    profileCgpa: document.getElementById('profile-cgpa'),
    profileLinkedin: document.getElementById('profile-linkedin'),
    profileGithub: document.getElementById('profile-github'),
    profileResume: document.getElementById('profile-resume'),
    profileResumeStatus: document.getElementById('profile-resume-status'),
    jobSearchForm: document.getElementById('job-search-form'),
    searchTitle: document.getElementById('search-title'),
    searchCompany: document.getElementById('search-company'),
    searchLocation: document.getElementById('search-location'),
    searchSkills: document.getElementById('search-skills'),
    searchMinSalary: document.getElementById('search-min-salary'),
    searchMaxSalary: document.getElementById('search-max-salary'),
    searchResetBtn: document.getElementById('search-reset-btn'),
    studentJobsList: document.getElementById('student-jobs-list'),
    studentAppsList: document.getElementById('student-applications-list'),
    
    // Recruiter Elements
    jobForm: document.getElementById('job-form'),
    jobFormTitle: document.getElementById('job-form-title'),
    jobIdInput: document.getElementById('job-id'),
    jobTitleInput: document.getElementById('job-title'),
    jobCompanyInput: document.getElementById('job-company'),
    jobSalaryInput: document.getElementById('job-salary'),
    jobSubmitBtn: document.getElementById('job-submit-btn'),
    cancelEditBtn: document.getElementById('cancel-edit-btn'),
    recruiterJobsList: document.getElementById('recruiter-jobs-list'),
    recruiterAppsList: document.getElementById('recruiter-applications-list'),
    
    // Admin Stats & Lists
    statStudents: document.getElementById('stat-students'),
    statJobs: document.getElementById('stat-jobs'),
    statApplications: document.getElementById('stat-applications'),
    adminStudentsList: document.getElementById('admin-students-list'),
    adminJobsList: document.getElementById('admin-jobs-list')
};

// JWT Decoder
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

// Toast Helper (SweetAlert2)
function showToast(message, type = 'info') {
    const iconMap = {
        'success': 'success',
        'error': 'error',
        'info': 'info'
    };
    const isLight = document.body.classList.contains('light-mode');
    Swal.fire({
        toast: true,
        position: 'bottom-end',
        icon: iconMap[type] || 'info',
        title: message,
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        background: isLight ? '#ffffff' : '#1e1e2d',
        color: isLight ? '#000000' : '#ffffff'
    });
}

// Fetch wrapper with JWT headers and error handling
async function apiFetch(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    
    if (state.token) {
        options.headers['Authorization'] = `Bearer ${state.token}`;
    }
    
    if (options.body && !(options.body instanceof FormData)) {
        options.headers['Content-Type'] = 'application/json';
        if (typeof options.body !== 'string') {
            options.body = JSON.stringify(options.body);
        }
    }
    
    try {
        const response = await fetch(url, options);
        
        if (response.status === 401 || response.status === 403) {
            logout();
            showToast('Session expired or unauthorized', 'error');
            throw new Error('Unauthorized');
        }
        
        const contentType = response.headers.get('content-type');
        const text = await response.text();
        
        if (!response.ok) {
            throw new Error(text || `HTTP error ${response.status}`);
        }
        
        if (!text) {
            return null;
        }

        if (contentType && contentType.includes('application/json')) {
            return JSON.parse(text);
        }
        return text;
    } catch (err) {
        console.error(`API Error on ${url}:`, err);
        throw err;
    }
}

// Init Application
function init() {
    setupEventListeners();
    
    if (state.token) {
        const claims = parseJwt(state.token);
        if (claims && claims.sub && claims.role) {
            state.user = {
                username: claims.sub,
                role: claims.role
            };
            showDashboard();
        } else {
            logout();
        }
    } else {
        showAuth();
    }
}

// Event Listeners
function setupEventListeners() {
    // Auth Toggles
    el.toggleToRegister.addEventListener('click', () => {
        el.loginForm.classList.add('hidden');
        el.registerForm.classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Create a new account to join CareerLink';
    });
    
    el.toggleToLogin.addEventListener('click', () => {
        el.registerForm.classList.add('hidden');
        el.loginForm.classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Welcome back! Please login to your account';
    });
    
    // Auth Forms Submission
    el.loginForm.addEventListener('submit', handleLogin);
    el.registerForm.addEventListener('submit', handleRegister);
    el.logoutBtn.addEventListener('click', logout);
    
    // Student Forms Submission
    el.profileForm.addEventListener('submit', handleSaveProfile);
    el.jobSearchForm.addEventListener('submit', handleJobSearch);
    el.searchResetBtn.addEventListener('click', resetJobSearch);
    
    // Recruiter Forms Submission
    el.jobForm.addEventListener('submit', handleJobSubmit);
    el.cancelEditBtn.addEventListener('click', resetJobForm);
    
    // Theme Toggle
    document.getElementById('theme-toggle').addEventListener('click', () => {
        document.body.classList.toggle('light-mode');
        const isLight = document.body.classList.contains('light-mode');
        localStorage.setItem('theme', isLight ? 'light' : 'dark');
        if (window.adminChartInstance) {
            window.adminChartInstance.options.plugins.legend.labels.color = isLight ? '#000' : '#fff';
            window.adminChartInstance.options.scales.x.ticks.color = isLight ? '#000' : '#fff';
            window.adminChartInstance.options.scales.y.ticks.color = isLight ? '#000' : '#fff';
            window.adminChartInstance.options.scales.y.grid.color = isLight ? 'rgba(0,0,0,0.1)' : 'rgba(255,255,255,0.1)';
            window.adminChartInstance.update();
        }
    });
    
    // Init theme
    if (localStorage.getItem('theme') === 'light') {
        document.body.classList.add('light-mode');
    }
}

// Screen Routing
function showAuth() {
    el.authSection.classList.remove('hidden');
    el.appSection.classList.add('hidden');
}

function showDashboard() {
    el.authSection.classList.add('hidden');
    el.appSection.classList.remove('hidden');
    
    const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(state.user.username)}&background=random&color=fff&rounded=true`;
    el.navUserInfo.innerHTML = `<img src="${avatarUrl}" alt="Avatar" style="vertical-align: middle;"> ${state.user.username} (${state.user.role})`;
    
    
    // Hide all dashboards first
    el.studentDash.classList.add('hidden');
    el.recruiterDash.classList.add('hidden');
    el.adminDash.classList.add('hidden');
    
    if (state.user.role === 'STUDENT') {
        el.studentDash.classList.remove('hidden');
        loadStudentDashboard();
    } else if (state.user.role === 'RECRUITER') {
        el.recruiterDash.classList.remove('hidden');
        loadRecruiterDashboard();
    } else if (state.user.role === 'ADMIN') {
        el.adminDash.classList.remove('hidden');
        loadAdminDashboard();
    }
    
    // GSAP Animation
    gsap.fromTo('.dashboard-view:not(.hidden) .dashboard-grid', 
        {opacity: 0, y: 30}, 
        {opacity: 1, y: 0, duration: 0.6, ease: "power2.out"}
    );
}

// Logout
function logout() {
    localStorage.removeItem('token');
    state.token = null;
    state.user = null;
    state.studentProfile = null;
    state.jobs = [];
    state.applications = [];
    state.students = [];
    
    // Clear forms
    el.loginForm.reset();
    el.registerForm.reset();
    el.profileForm.reset();
    resetJobForm();
    
    showAuth();
}

// ==========================================================================
// AUTH HANDLERS
// ==========================================================================

async function handleLogin(e) {
    e.preventDefault();
    const spinner = document.getElementById('login-spinner');
    const text = document.querySelector('#login-btn span');
    
    spinner.classList.remove('hidden');
    text.classList.add('hidden');
    
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const token = await apiFetch('/auth/login', {
            method: 'POST',
            body: { username, password }
        });
        
        state.token = token;
        localStorage.setItem('token', token);
        
        const claims = parseJwt(token);
        state.user = {
            username: claims.sub,
            role: claims.role
        };
        
        showToast('Successfully logged in!', 'success');
        showDashboard();
    } catch (err) {
        showToast(err.message || 'Login failed. Please check credentials.', 'error');
    } finally {
        spinner.classList.add('hidden');
        text.classList.remove('hidden');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const spinner = document.getElementById('register-spinner');
    const text = document.querySelector('#register-btn span');
    
    spinner.classList.remove('hidden');
    text.classList.add('hidden');
    
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const role = document.getElementById('register-role').value;
    
    try {
        const msg = await apiFetch('/auth/register', {
            method: 'POST',
            body: { username, email, password, role }
        });
        
        showToast(msg || 'Registration successful! Please login.', 'success');
        el.registerForm.classList.add('hidden');
        el.loginForm.classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Welcome back! Please login to your account';
    } catch (err) {
        showToast(err.message || 'Registration failed. Username might be taken.', 'error');
    } finally {
        spinner.classList.add('hidden');
        text.classList.remove('hidden');
    }
}

// ==========================================================================
// STUDENT ACTIONS & RENDERING
// ==========================================================================

async function loadStudentDashboard() {
    try {
        const profile = await apiFetch('/students/me');
        if (profile && profile.id) {
            state.studentProfile = profile;
            populateProfileForm(profile);
        } else {
            state.studentProfile = null;
        }

        state.jobs = await fetchJobs();
        state.applications = await apiFetch('/apply/my') || [];

        renderStudentJobs();
        renderStudentApplications();
    } catch (err) {
        showToast('Error loading student dashboard', 'error');
    }
}

function populateProfileForm(profile) {
    el.profileEmail.value = profile.email || '';
    el.profilePhone.value = profile.phone || '';
    el.profileBranch.value = profile.branch || '';
    el.profileYear.value = profile.year || '';
    el.profileCollege.value = profile.college || '';
    el.profileSkills.value = profile.skills || '';
    el.profileCgpa.value = profile.cgpa || '';
    el.profileLinkedin.value = profile.linkedIn || '';
    el.profileGithub.value = profile.github || '';
    el.profileResumeStatus.textContent = profile.resumeUrl
        ? `Current resume: ${profile.resumeUrl}`
        : 'No resume uploaded yet.';
}

async function fetchJobs() {
    const params = new URLSearchParams();
    if (el.searchTitle.value.trim()) params.set('title', el.searchTitle.value.trim());
    if (el.searchCompany.value.trim()) params.set('company', el.searchCompany.value.trim());
    if (el.searchLocation.value.trim()) params.set('location', el.searchLocation.value.trim());
    if (el.searchSkills.value.trim()) params.set('skills', el.searchSkills.value.trim());
    if (el.searchMinSalary.value) params.set('minSalary', el.searchMinSalary.value);
    if (el.searchMaxSalary.value) params.set('maxSalary', el.searchMaxSalary.value);
    params.set('size', '50');

    const hasFilters = params.has('title') || params.has('company') || params.has('location')
        || params.has('skills') || params.has('minSalary') || params.has('maxSalary');

    if (hasFilters) {
        const page = await apiFetch(`/jobs/search?${params.toString()}`);
        return page?.content || [];
    }

    return await apiFetch('/jobs') || [];
}

async function handleJobSearch(e) {
    e.preventDefault();
    try {
        state.jobs = await fetchJobs();
        renderStudentJobs();
        showToast(`Found ${state.jobs.length} job(s)`, 'success');
    } catch (err) {
        showToast(err.message || 'Job search failed', 'error');
    }
}

function resetJobSearch() {
    el.jobSearchForm.reset();
    loadStudentDashboard();
}

async function handleSaveProfile(e) {
    e.preventDefault();

    const profileData = {
        name: state.user.username,
        email: el.profileEmail.value.trim(),
        phone: el.profilePhone.value.trim(),
        branch: el.profileBranch.value.trim(),
        year: el.profileYear.value,
        college: el.profileCollege.value.trim(),
        skills: el.profileSkills.value.trim(),
        cgpa: el.profileCgpa.value ? parseFloat(el.profileCgpa.value) : 0,
        linkedIn: el.profileLinkedin.value.trim(),
        github: el.profileGithub.value.trim()
    };

    try {
        const profile = await apiFetch('/students/me', {
            method: 'POST',
            body: profileData
        });

        state.studentProfile = profile;

        if (el.profileResume.files.length > 0) {
            const formData = new FormData();
            formData.append('file', el.profileResume.files[0]);
            const updated = await apiFetch('/students/me/resume', {
                method: 'POST',
                body: formData
            });
            state.studentProfile = updated;
            el.profileResume.value = '';
        }

        populateProfileForm(state.studentProfile);
        showToast('Profile saved successfully!', 'success');
        loadStudentDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to save profile', 'error');
    }
}

function renderStudentJobs() {
    el.studentJobsList.innerHTML = '';
    
    if (state.jobs.length === 0) {
        el.studentJobsList.innerHTML = '<p class="card-desc">No active job postings available.</p>';
        return;
    }
    
    state.jobs.forEach(job => {
        // Find if user already applied to this job
        const applied = state.applications.find(app => getApplicationJobId(app) === job.id);
        
        const card = document.createElement('div');
        card.className = 'job-card';
        
        let actionBtnHtml = '';
        if (!state.studentProfile) {
            actionBtnHtml = `<button class="btn btn-secondary btn-block" disabled>Create profile to apply</button>`;
        } else if (applied) {
            const statusClass = getStatusClass(applied.status);
            actionBtnHtml = `<div class="badge ${statusClass} btn-block" style="text-align: center; justify-content: center; display: flex;">Applied (${applied.status})</div>`;
        } else {
            actionBtnHtml = `<button class="btn btn-primary btn-block" onclick="applyForJob(${job.id})">Apply Now</button>`;
        }
        
        card.innerHTML = `
            <div class="job-card-header">
                <h4>${escapeHtml(job.title)}</h4>
                <div class="job-company">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    <span>${escapeHtml(job.company)}</span>
                </div>
                ${job.location ? `<p class="card-desc">${escapeHtml(job.location)}</p>` : ''}
                ${job.skills ? `<p class="card-desc">${escapeHtml(job.skills)}</p>` : ''}
            </div>
            <div class="job-salary-badge">${formatSalary(job.salary)}/yr</div>
            ${actionBtnHtml}
        `;
        el.studentJobsList.appendChild(card);
    });
}

async function applyForJob(jobId) {
    try {
        await apiFetch(`/apply?jobId=${encodeURIComponent(jobId)}`, {
            method: 'POST'
        });
        showToast('Application submitted successfully!', 'success');
        loadStudentDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to submit application', 'error');
    }
}

function renderStudentApplications() {
    el.studentAppsList.innerHTML = '';
    
    if (state.applications.length === 0) {
        el.studentAppsList.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-secondary);">You have not applied for any jobs yet.</td></tr>';
        return;
    }
    
    state.applications.forEach(app => {
        const job = app.job || state.jobs.find(j => j.id === getApplicationJobId(app)) || { title: 'Unknown Job', company: 'Unknown Company', salary: 0 };
        const tr = document.createElement('tr');
        const statusClass = getStatusClass(app.status);
        
        tr.innerHTML = `
            <td><strong>${escapeHtml(job.title)}</strong></td>
            <td>${escapeHtml(job.company)}</td>
            <td>${formatSalary(job.salary)}</td>
            <td><span class="badge ${statusClass}">${app.status}</span></td>
        `;
        el.studentAppsList.appendChild(tr);
    });
}

// ==========================================================================
// RECRUITER ACTIONS & RENDERING
// ==========================================================================

async function loadRecruiterDashboard() {
    try {
        state.jobs = await apiFetch('/jobs/my') || [];
        state.students = await apiFetch('/students') || [];
        state.applications = await apiFetch('/apply/recruiter/my') || [];
        
        renderRecruiterJobs();
        renderRecruiterApplications();
    } catch (err) {
        showToast('Error loading recruiter dashboard', 'error');
    }
}

function renderRecruiterJobs() {
    el.recruiterJobsList.innerHTML = '';
    
    const recruiterJobs = state.jobs;
    if (recruiterJobs.length === 0) {
        el.recruiterJobsList.innerHTML = '<p style="color: var(--text-secondary);">No active job postings. Create one on the left!</p>';
        return;
    }
    
    recruiterJobs.forEach(job => {
        const div = document.createElement('div');
        div.className = 'job-list-item';
        div.innerHTML = `
            <div class="job-list-details">
                <h4>${escapeHtml(job.title)}</h4>
                <p>${escapeHtml(job.company)} &bull; ${formatSalary(job.salary)}/yr</p>
            </div>
            <div class="btn-group">
                <button class="btn btn-secondary btn-sm" onclick="populateEditJob(${job.id})">Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteJob(${job.id})">Delete</button>
            </div>
        `;
        el.recruiterJobsList.appendChild(div);
    });
}

function renderRecruiterApplications() {
    el.recruiterAppsList.innerHTML = '';
    
    if (state.applications.length === 0) {
        el.recruiterAppsList.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-secondary);">No applications received yet.</td></tr>';
        return;
    }
    
    state.applications.forEach(app => {
        const student = app.student || state.students.find(s => s.id === getApplicationStudentId(app)) || { name: 'Unknown Student', skills: 'N/A', cgpa: 0.0 };
        const job = app.job || state.jobs.find(j => j.id === getApplicationJobId(app)) || { title: 'Unknown Job', company: 'Unknown Company' };
        
        const tr = document.createElement('tr');
        const statusClass = getStatusClass(app.status);
        
        tr.innerHTML = `
            <td><strong>${escapeHtml(student.name)}</strong></td>
            <td>${escapeHtml(student.skills)}</td>
            <td>${Number(student.cgpa || 0).toFixed(2)}</td>
            <td>${escapeHtml(job.title)}</td>
            <td>${escapeHtml(job.company)}</td>
            <td><span class="badge ${statusClass}">${app.status}</span></td>
            <td>
                <select class="status-action-select" onchange="updateApplicationStatus(${app.id}, this.value)">
                    <option value="APPLIED" ${app.status === 'APPLIED' ? 'selected' : ''}>Applied</option>
                    <option value="UNDER_REVIEW" ${app.status === 'UNDER_REVIEW' ? 'selected' : ''}>Under Review</option>
                    <option value="INTERVIEW" ${app.status === 'INTERVIEW' ? 'selected' : ''}>Interview</option>
                    <option value="SELECTED" ${app.status === 'SELECTED' ? 'selected' : ''}>Selected</option>
                    <option value="REJECTED" ${app.status === 'REJECTED' ? 'selected' : ''}>Rejected</option>
                </select>
            </td>
        `;
        el.recruiterAppsList.appendChild(tr);
    });
}

async function handleJobSubmit(e) {
    e.preventDefault();
    const jobId = el.jobIdInput.value;
    const title = el.jobTitleInput.value;
    const company = el.jobCompanyInput.value;
    const salary = parseFloat(el.jobSalaryInput.value);
    
    const isEdit = !!jobId;
    const url = isEdit ? `/jobs/${jobId}` : '/jobs';
    const method = isEdit ? 'PUT' : 'POST';
    
    try {
        await apiFetch(url, {
            method,
            body: { title, company, salary }
        });
        
        showToast(isEdit ? 'Job updated successfully!' : 'Job posted successfully!', 'success');
        resetJobForm();
        loadRecruiterDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to save job details', 'error');
    }
}

function populateEditJob(id) {
    const job = state.jobs.find(j => j.id === id);
    if (!job) return;
    
    el.jobIdInput.value = job.id;
    el.jobTitleInput.value = job.title;
    el.jobCompanyInput.value = job.company;
    el.jobSalaryInput.value = job.salary;
    
    el.jobFormTitle.textContent = 'Edit Job Details';
    el.jobSubmitBtn.textContent = 'Save Changes';
    el.cancelEditBtn.classList.remove('hidden');
}

function resetJobForm() {
    el.jobForm.reset();
    el.jobIdInput.value = '';
    el.jobFormTitle.textContent = 'Post a New Job';
    el.jobSubmitBtn.textContent = 'Post Job';
    el.cancelEditBtn.classList.add('hidden');
}

async function deleteJob(id) {
    if (!confirm('Are you sure you want to delete this job posting?')) return;
    
    try {
        await apiFetch(`/jobs/${id}`, {
            method: 'DELETE'
        });
        showToast('Job posting deleted!', 'success');
        loadRecruiterDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to delete job', 'error');
    }
}

async function updateApplicationStatus(appId, newStatus) {
    try {
        await apiFetch(`/apply/${appId}/status?status=${newStatus}`, {
            method: 'PUT'
        });
        showToast(`Application status updated to ${newStatus}!`, 'success');
        loadRecruiterDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to update application status', 'error');
    }
}

// ==========================================================================
// ADMIN ACTIONS & RENDERING
// ==========================================================================

async function loadAdminDashboard() {
    try {
        // Fetch all jobs, students, and applications
        state.students = await apiFetch('/students') || [];
        state.jobs = await apiFetch('/jobs') || [];
        state.applications = await apiFetch('/apply/all') || [];
        
        // Render stats counters
        el.statStudents.textContent = state.students.length;
        el.statJobs.textContent = state.jobs.length;
        el.statApplications.textContent = state.applications.length;
        
        renderAdminChart();
        renderAdminStudents();
        renderAdminJobs();
    } catch (err) {
        showToast('Error loading admin dashboard', 'error');
    }
}

function renderAdminStudents() {
    el.adminStudentsList.innerHTML = '';
    
    if (state.students.length === 0) {
        el.adminStudentsList.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-secondary);">No students registered.</td></tr>';
        return;
    }
    
    state.students.forEach(student => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${student.id}</td>
            <td><strong>${escapeHtml(student.name)}</strong></td>
            <td>${escapeHtml(student.skills)}</td>
            <td>${student.cgpa.toFixed(2)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="adminDeleteStudent(${student.id})">Delete Profile</button>
            </td>
        `;
        el.adminStudentsList.appendChild(tr);
    });
}

function renderAdminJobs() {
    el.adminJobsList.innerHTML = '';
    
    if (state.jobs.length === 0) {
        el.adminJobsList.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-secondary);">No active job postings.</td></tr>';
        return;
    }
    
    state.jobs.forEach(job => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${job.id}</td>
            <td><strong>${escapeHtml(job.title)}</strong></td>
            <td>${escapeHtml(job.company)}</td>
            <td>${formatSalary(job.salary)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="adminDeleteJob(${job.id})">Delete Job</button>
            </td>
        `;
        el.adminJobsList.appendChild(tr);
    });
}

async function adminDeleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student profile?')) return;
    
    try {
        await apiFetch(`/admin/students/${id}`, {
            method: 'DELETE'
        });
        showToast('Student profile deleted!', 'success');
        loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to delete student', 'error');
    }
}

async function adminDeleteJob(id) {
    if (!confirm('Are you sure you want to delete this job posting?')) return;
    
    try {
        await apiFetch(`/admin/jobs/${id}`, {
            method: 'DELETE'
        });
        showToast('Job posting deleted!', 'success');
        loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to delete job', 'error');
    }
}

// ==========================================================================
// HELPERS
// ==========================================================================

function getStatusClass(status) {
    switch (status) {
        case 'APPLIED': return 'badge-applied';
        case 'UNDER_REVIEW': return 'badge-review';
        case 'INTERVIEW': return 'badge-interview';
        case 'SELECTED': return 'badge-selected';
        case 'REJECTED': return 'badge-rejected';
        default: return 'badge-review';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function getApplicationJobId(app) {
    return app.jobId || (app.job && app.job.id);
}

function getApplicationStudentId(app) {
    return app.studentId || (app.student && app.student.id);
}

function formatSalary(value) {
    const salary = Number(value || 0);
    return salary > 0 ? `$${salary.toLocaleString()}` : 'Not disclosed';
}

function renderAdminChart() {
    const ctx = document.getElementById('adminChart');
    if (!ctx) return;
    
    if (window.adminChartInstance) {
        window.adminChartInstance.destroy();
    }
    
    const appliedCount = state.applications.filter(a => a.status === 'APPLIED').length;
    const reviewCount = state.applications.filter(a => a.status === 'UNDER_REVIEW').length;
    const interviewCount = state.applications.filter(a => a.status === 'INTERVIEW').length;
    const selectedCount = state.applications.filter(a => a.status === 'SELECTED').length;
    const rejectedCount = state.applications.filter(a => a.status === 'REJECTED').length;

    const isLight = document.body.classList.contains('light-mode');
    
    window.adminChartInstance = new Chart(ctx.getContext('2d'), {
        type: 'bar',
        data: {
            labels: ['Applied', 'Under Review', 'Interview', 'Selected', 'Rejected'],
            datasets: [{
                label: 'Applications by Status',
                data: [appliedCount, reviewCount, interviewCount, selectedCount, rejectedCount],
                backgroundColor: [
                    'rgba(245, 158, 11, 0.6)',
                    'rgba(59, 130, 246, 0.6)',
                    'rgba(168, 85, 247, 0.6)',
                    'rgba(16, 185, 129, 0.6)',
                    'rgba(239, 68, 68, 0.6)'
                ],
                borderColor: [
                    '#f59e0b',
                    '#3b82f6',
                    '#a855f7',
                    '#10b981',
                    '#ef4444'
                ],
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: isLight ? '#000' : '#fff' } }
            },
            scales: {
                y: { 
                    beginAtZero: true, 
                    ticks: { color: isLight ? '#000' : '#fff', stepSize: 1 }, 
                    grid: { color: isLight ? 'rgba(0,0,0,0.1)' : 'rgba(255,255,255,0.1)' } 
                },
                x: { 
                    ticks: { color: isLight ? '#000' : '#fff' }, 
                    grid: { display: false } 
                }
            }
        }
    });
}

// Run application on window load
window.addEventListener('load', init);
