const API_BASE_URL = '';

function getStoredToken() {
    return localStorage.getItem('token') || localStorage.getItem('jwt_token');
}

function clearStoredAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
}

const api = {
    async fetch(endpoint, options = {}) {
        const token = getStoredToken();

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
            const text = await response.text();

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    clearStoredAuth();
                    window.location.href = '/index.html';
                    throw new Error('Unauthorized. Please login again.');
                }
                throw new Error(text || 'API Request failed');
            }

            try {
                return JSON.parse(text);
            } catch (e) {
                return text;
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

function getRole() {
    const token = getStoredToken();
    if (!token) {
        return localStorage.getItem('user_role');
    }
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.role || localStorage.getItem('user_role');
    } catch (e) {
        return localStorage.getItem('user_role');
    }
}

function isLoggedIn() {
    return !!getStoredToken();
}

function logout() {
    clearStoredAuth();
    window.location.href = '/index.html';
}

function checkAuth(requiredRole) {
    if (!isLoggedIn()) {
        window.location.href = '/index.html';
        return;
    }

    if (requiredRole && getRole() !== requiredRole) {
        window.location.href = '/index.html';
    }
}
