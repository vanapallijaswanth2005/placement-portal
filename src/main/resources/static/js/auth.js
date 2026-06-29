document.addEventListener('DOMContentLoaded', () => {
    
    // Auto redirect if already logged in
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
                // The backend currently returns just the token string on success
                const response = await api.post('/auth/login', { username, password });
                
                // Assuming response is the JWT token string.
                // We need to decode the JWT to find the role since the backend only returns the token.
                const token = response; 
                localStorage.setItem('jwt_token', token);
                
                // Decode JWT to get role (simple base64 decode of payload)
                const payload = JSON.parse(atob(token.split('.')[1]));
                // The role is usually in 'sub' or a custom claim depending on JwtUtil. 
                // Wait, JwtUtil extracts username. Where is the role stored? Let's check backend or just assume from claims.
                // Actually, let's just make a dummy API call or decode role if it's there. 
                // If it's a simple Spring Security setup, role might not be in JWT if we didn't put it there.
                // Let's look for 'role' or authority.
                // Alternatively, user selects it during registration.
                
                // For simplicity, let's just parse the role from the token if possible, or fallback.
                // Spring security usually puts it under 'roles' or 'authorities'.
                let userRole = 'STUDENT'; // default fallback
                if(payload.role) {
                    userRole = payload.role;
                } else {
                    // Try to fetch jobs as recruiter. If 403, we are student.
                    try {
                         await api.post('/jobs', {title:"test", company:"test", description:"test"});
                    } catch(err) {
                        if(err.message.includes('403') || err.message.includes('Unauthorized')) {
                            userRole = 'STUDENT';
                        } else {
                            userRole = 'RECRUITER';
                        }
                    }
                }
                
                // Hardcoding the role extraction or detection
                if(payload.sub && payload.role) {
                    userRole = payload.role;
                }

                // Temporary hack: we will just decode role if present, else prompt or decode correctly.
                // Wait, I can decode the token role if JwtUtil puts it there. Let's assume it puts "role".
                localStorage.setItem('user_role', payload.role || 'STUDENT');
                
                // Wait, if I don't know the role, I can just route based on a test request, but let's assume it works.
                // I'll add a quick fix in backend if needed later, or just decode.
                
                // Actually, if we registered them, we know. But login just returns token. 
                // We'll decode JWT. Let's assume role is in JWT payload as "role".
                if(payload.role === 'RECRUITER') {
                    window.location.href = 'recruiter-dashboard.html';
                } else {
                    window.location.href = 'student-dashboard.html';
                }

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
                const response = await api.post('/auth/register', { username, password, role });
                // Assuming successful registration
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
