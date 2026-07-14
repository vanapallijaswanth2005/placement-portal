const state = { students: [], jobs: [], applications: [] };

function getApplicationStudentId(app) {
    return app.studentId || (app.student && app.student.id);
}

function getApplicationJobId(app) {
    return app.jobId || (app.job && app.job.id);
}

const app = {
    id: 1,
    status: 'APPLIED',
    student: { id: 1, name: 'John Doe', skills: 'Java', cgpa: 9.0 },
    job: { id: 1, title: 'Dev', company: 'Tech' }
};

state.applications = [app];

state.applications.forEach(app => {
    const student = app.student || state.students.find(s => s.id === getApplicationStudentId(app)) || { name: 'Unknown Student', skills: 'N/A', cgpa: 0.0 };
    const job = app.job || state.jobs.find(j => j.id === getApplicationJobId(app)) || { title: 'Unknown Job', company: 'Unknown Company' };
    
    console.log("Student:", student);
    console.log("Job:", job);
});
