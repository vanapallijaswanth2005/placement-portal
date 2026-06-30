document.addEventListener('DOMContentLoaded', () => {
    // Only allow students
    checkAuth('STUDENT');

    loadJobs();
});

async function loadJobs() {
    const jobList = document.getElementById('jobList');
    jobList.innerHTML = '<p>Loading jobs...</p>';

    try {
        const jobs = await api.get('/jobs');
        
        if (jobs.length === 0) {
            jobList.innerHTML = '<p>No jobs available at the moment.</p>';
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
                <div class="job-meta">
                    <span>${escapeHTML(job.location || 'Remote')}</span>
                    <span>${escapeHTML(job.salary || 'Competitive')}</span>
                </div>
                <div class="job-desc">${escapeHTML(job.description || '')}</div>
                <div class="job-actions">
                    <button class="btn btn-primary btn-sm w-100" onclick="applyForJob(${job.id})">Apply Now</button>
                </div>
            `;
            jobList.appendChild(card);
        });

    } catch (err) {
        jobList.innerHTML = `<p class="error-text">Failed to load jobs: ${err.message}</p>`;
    }
}

async function applyForJob(jobId) {
    if(!confirm("Are you sure you want to apply for this job?")) return;

    try {
        await api.fetch(`/apply?jobId=${encodeURIComponent(jobId)}`, { method: 'POST' });
        alert('Successfully applied!');
        
    } catch (err) {
        alert(`Error applying: ${err.message}`);
    }
}

function escapeHTML(str) {
    const p = document.createElement("p");
    p.appendChild(document.createTextNode(str));
    return p.innerHTML;
}
