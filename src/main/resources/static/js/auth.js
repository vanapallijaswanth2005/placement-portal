document.addEventListener('DOMContentLoaded', () => {

    if (isLoggedIn() && (window.location.pathname.endsWith('login.html') || window.location.pathname.endsWith('register.html'))) {
        const role = getRole();
        window.location.href = role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'student-dashboard.html';
    }

    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.style.display = 'none';

            const btn = e.target.querySelector('button');
            btn.textContent = 'Logging in...';
            btn.disabled = true;

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const token = await api.post('/auth/login', { username, password });
                const payload = JSON.parse(atob(token.split('.')[1]));
                const role = payload.role || 'STUDENT';

                localStorage.setItem('jwt_token', token);
                localStorage.setItem('user_role', role);

                window.location.href = role === 'RECRUITER'
                    ? 'recruiter-dashboard.html'
                    : 'student-dashboard.html';
            } catch (err) {
                errorDiv.textContent = err.message || 'Login failed. Please check your credentials.';
                errorDiv.style.display = 'block';
                btn.textContent = 'Login';
                btn.disabled = false;
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const errorDiv = document.getElementById('regErrorMessage');
            errorDiv.style.display = 'none';

            const btn = e.target.querySelector('button');
            btn.textContent = 'Registering...';
            btn.disabled = true;

            const username = document.getElementById('reg-username').value;
            const password = document.getElementById('reg-password').value;
            const role = document.getElementById('reg-role').value;

            try {
                await api.post('/auth/register', { username, password, role });
                alert('Registration successful! Please login.');
                window.location.href = 'login.html';
            } catch (err) {
                errorDiv.textContent = err.message || 'Registration failed.';
                errorDiv.style.display = 'block';
                btn.textContent = 'Register';
                btn.disabled = false;
            }
        });
    }
});
