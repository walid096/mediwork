// Login, logout, token refresh, etc..
import api from '../config/api';

export const authApi = {
    login: async (credentials) => {
        const response = await api.post('/auth/login', credentials);
        return response.data;
    },

    register: async (userData) => {
        const response = await api.post('/auth/register', userData);
        return response.data;
    },

    logout: async (refreshToken) => {
        try {
            // Revoke refresh token on backend if provided
            if (refreshToken) {
                await api.post('/auth/logout', { refreshToken });
            }
        } catch (error) {
            console.error('Backend logout failed:', error);
            // Continue with local cleanup even if backend fails
        } finally {
            // Always clear local storage
            this.clearAuthData();
        }
    },

    refreshToken: async (refreshToken) => {
        try {
            const response = await api.post('/auth/refresh', { refreshToken });
            return response.data;
        } catch (error) {
            console.error('Token refresh failed:', error);
            throw error;
        }
    },

    getCurrentUser: () => {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    getAccessToken: () => {
        return localStorage.getItem('token');
    },

    getRefreshToken: () => {
        return localStorage.getItem('refreshToken');
    },

    setAuthData: (accessToken, refreshToken, user) => {
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));
    },

    updateAccessToken: (newAccessToken) => {
        localStorage.setItem('token', newAccessToken);
    },

    updateRefreshToken: (newRefreshToken) => {
        localStorage.setItem('refreshToken', newRefreshToken);
    },

    clearAuthData: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    },

    isTokenExpired: (token) => {
        if (!token) return true;
        
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            return payload.exp < currentTime;
        } catch (error) {
            console.error('Error parsing token:', error);
            return true;
        }
    },

    getTokenExpiryTime: (token) => {
        if (!token) return null;
        
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000; // Convert to milliseconds
        } catch (error) {
            console.error('Error parsing token:', error);
            return null;
        }
    },

    validateToken: async (refreshToken) => {
        try {
            const response = await api.get('/auth/validate-token', {
                params: { refreshToken }
            });
            return response.status === 200;
        } catch (error) {
            return false;
        }
    }
};