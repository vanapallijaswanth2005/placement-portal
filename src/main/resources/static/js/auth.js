document.addEventListener('DOMContentLoaded', () => {
    if (isLoggedIn()) {
        window.location.href = '/index.html';
    }
});
