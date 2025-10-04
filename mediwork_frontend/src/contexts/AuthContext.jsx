import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { hasPermission as checkPermission } from '../utils/permissions';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [refreshToken, setRefreshToken] = useState(null);

    useEffect(() => {
        const currentUser = authApi.getCurrentUser();
        const storedRefreshToken = authApi.getRefreshToken();
        
        if (currentUser && storedRefreshToken) {
            setUser(currentUser);
            setRefreshToken(storedRefreshToken);
        }
        setLoading(false);
    }, []);

    useEffect(() => {
        const checkTokenExpiry = async () => {
            if (refreshToken && authApi.isTokenExpired(authApi.getAccessToken())) {
                try {
                    await refreshAccessToken();
                } catch (error) {
                    console.error("Auto-refresh failed, logging out");
                    await logout();
                }
            }
        };
        
        const interval = setInterval(checkTokenExpiry, 60000);
        return () => clearInterval(interval);
    }, [refreshToken]);

    const login = async (credentials) => {
        try {
            const response = await authApi.login(credentials);
            const { accessToken, refreshToken: newRefreshToken, ...userData } = response;
            
            authApi.setAuthData(accessToken, newRefreshToken, userData);
            setUser(userData);
            setRefreshToken(newRefreshToken);
            
            return { success: true, user: userData };
        } catch (error) {
            return {
                success: false,
                error: error.response?.data?.message || 'Login failed'
            };
        }
    };

    const register = async (userData) => {
        try {
            const response = await authApi.register(userData);
            return { success: true, user: response.data };
        } catch (error) {
            return {
                success: false,
                error: error.response?.data?.message || 'Registration failed'
            };
        }
    };

    const logout = async () => {
        try {
            if (refreshToken) {
                await authApi.logout(refreshToken);
            }
        } catch (error) {
            console.error("Logout error:", error);
        } finally {
            authApi.clearAuthData();
            setUser(null);
            setRefreshToken(null);
        }
    };

    const refreshAccessToken = async () => {
        if (!refreshToken) {
            throw new Error('No refresh token available');
        }

        try {
            const response = await authApi.refreshToken(refreshToken);
            const { accessToken, refreshToken: newRefreshToken } = response;
            
            authApi.updateAccessToken(accessToken);
            if (newRefreshToken) {
                authApi.updateRefreshToken(newRefreshToken);
                setRefreshToken(newRefreshToken);
            }
            
            return accessToken;
        } catch (error) {
            console.error("Token refresh failed:", error);
            await logout();
            throw error;
        }
    };

    const hasPermission = (permission) => {
        if (!user) return false;
        return checkPermission(user.role, permission);
    };

    const value = {
        user,
        login,
        register,
        logout,
        hasPermission,
        loading,
        refreshAccessToken,
        refreshToken
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};