const API_BASE_URL = ''; // using relative URL since frontend is served by backend

const api = {
    // Helper for fetch with Authorization
    async fetch(endpoint, options = {}) {
        const token = localStorage.getItem('jwt_token');
        
        const headers = {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
            ...options.headers
        };

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            
            // If text response (like in AuthController which returns String)
            const text = await response.text();
            
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    // Token expired or unauthorized
                    localStorage.removeItem('jwt_token');
                    localStorage.removeItem('user_role');
                    window.location.href = 'login.html';
                    throw new Error('Unauthorized. Please login again.');
                }
                throw new Error(text || 'API Request failed');
            }

            // Try to parse as JSON if it's not a plain string
            try {
                return JSON.parse(text);
            } catch (e) {
                return text; // Return plain text if not JSON
            }
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    get(endpoint) {
        return this.fetch(endpoint, { method: 'GET' });
    },

    post(endpoint, data) {
        return this.fetch(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(endpoint, data) {
        return this.fetch(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.fetch(endpoint, { method: 'DELETE' });
    }
};

// Utils
function getRole() {
    return localStorage.getItem('user_role');
}

function isLoggedIn() {
    return !!localStorage.getItem('jwt_token');
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
    window.location.href = 'login.html';
}

function checkAuth(requiredRole) {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return;
    }
    
    if (requiredRole && getRole() !== requiredRole) {
        // Redirect to their respective dashboard
        const role = getRole();
        if (role === 'STUDENT') window.location.href = 'student-dashboard.html';
        else if (role === 'RECRUITER') window.location.href = 'recruiter-dashboard.html';
        else window.location.href = 'index.html';
    }
}
