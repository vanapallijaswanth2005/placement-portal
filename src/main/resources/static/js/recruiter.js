document.addEventListener('DOMContentLoaded', () => {
    // Only allow recruiters
    checkAuth('RECRUITER');

    loadMyJobs();
    loadApplications();

    const postJobBtn = document.getElementById('postJobBtn');
    if (postJobBtn) {
        postJobBtn.addEventListener('click', () => {
            document.getElementById('jobModal').classList.add('active');
        });
    }

    const closeBtn = document.querySelector('.close-btn');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            document.getElementById('jobModal').classList.remove('active');
        });
    }

    const createJobForm = document.getElementById('createJobForm');
    if (createJobForm) {
        createJobForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button');
            btn.disabled = true;
            btn.textContent = 'Posting...';

            const jobData = {
                title: document.getElementById('jobTitle').value,
                company: document.getElementById('jobCompany').value,
                location: document.getElementById('jobLocation').value,
                salary: document.getElementById('jobSalary').value,
                description: document.getElementById('jobDescription').value
            };

            try {
                await api.post('/jobs', jobData);
                document.getElementById('jobModal').classList.remove('active');
                createJobForm.reset();
                loadMyJobs();
            } catch (err) {
                alert(`Error: ${err.message}`);
            } finally {
                btn.disabled = false;
                btn.textContent = 'Post Job';
            }
        });
    }
});

async function loadMyJobs() {
    const jobList = document.getElementById('recruiterJobs');
    if (!jobList) return;
    jobList.innerHTML = '<p>Loading...</p>';

    try {
        const jobs = await api.get('/jobs');
        
        if (jobs.length === 0) {
            jobList.innerHTML = '<p>You have not posted any jobs yet.</p>';
            return;
        }

        jobList.innerHTML = '';
        jobs.forEach(job => {
            const card = document.createElement('div');
            card.className = 'job-card';
            card.innerHTML = `
                <div class="job-header">
                    <h3 class="job-title">${escapeHTML(job.title)}</h3>
                </div>
                <div class="job-company">${escapeHTML(job.company)}</div>
                <div class="job-desc">${escapeHTML(job.description || '')}</div>
                <div class="job-actions">
                    <button class="btn btn-danger btn-sm" onclick="deleteJob(${job.id})">Delete</button>
                </div>
            `;
            jobList.appendChild(card);
        });
    } catch (err) {
        jobList.innerHTML = `<p class="error-text">Failed to load jobs: ${err.message}</p>`;
    }
}

async function loadApplications() {
    const appList = document.getElementById('applicationList');
    if (!appList) return;
    appList.innerHTML = '<div class="data-row"><p>Loading applications...</p></div>';

    try {
        const apps = await api.get('/apply/all');
        
        if (apps.length === 0) {
            appList.innerHTML = '<div class="data-row"><p>No applications received yet.</p></div>';
            return;
        }

        appList.innerHTML = '';
        apps.forEach(app => {
            const studentName = app.student ? app.student.name : 'Unknown Student';
            const jobTitle = app.job ? app.job.title : 'Unknown Job';
            const statusClass = `status-${app.status || 'PENDING'}`;
            
            const row = document.createElement('div');
            row.className = 'data-row';
            row.innerHTML = `
                <div class="col-2">${escapeHTML(studentName)}</div>
                <div class="col-3">${escapeHTML(jobTitle)}</div>
                <div class="col-1"><span class="status-badge ${statusClass}">${escapeHTML(app.status)}</span></div>
                <div class="col-2" style="display:flex; gap:0.5rem;">
                    <select onchange="updateStatus(${app.id}, this.value)" style="padding: 0.25rem; font-size: 0.8rem; background:rgba(0,0,0,0.5); color:white;">
                        <option value="PENDING" ${app.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="REVIEWING" ${app.status === 'REVIEWING' ? 'selected' : ''}>Reviewing</option>
                        <option value="ACCEPTED" ${app.status === 'ACCEPTED' ? 'selected' : ''}>Accept</option>
                        <option value="REJECTED" ${app.status === 'REJECTED' ? 'selected' : ''}>Reject</option>
                    </select>
                </div>
            `;
            appList.appendChild(row);
        });
    } catch (err) {
        appList.innerHTML = `<div class="data-row"><p class="error-text">Failed to load applications: ${err.message}</p></div>`;
    }
}

async function deleteJob(id) {
    if (!confirm('Are you sure you want to delete this job?')) return;
    try {
        await api.delete(`/jobs/${id}`);
        loadMyJobs();
    } catch (err) {
        alert('Failed to delete job: ' + err.message);
    }
}

async function updateStatus(appId, newStatus) {
    try {
        // Endpoint: PUT /apply/{id}/status?status={status}
        await api.put(`/apply/${appId}/status?status=${newStatus}`, {});
        loadApplications(); // reload to reflect changes visually
    } catch (err) {
        alert('Failed to update status: ' + err.message);
    }
}

function escapeHTML(str) {
    if(!str) return '';
    const p = document.createElement("p");
    p.appendChild(document.createTextNode(str));
    return p.innerHTML;
}
