//Admin user management calls (create, update users)
import api from '../config/api';

export const userApi = {
    getUsers: async () => {
        try {
            const response = await api.get('/admin/users');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des utilisateurs');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    createUser: async (userData) => {
        try {
            const response = await api.post('/admin/users', userData);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la création');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    updateUser: async (id, userData) => {
        try {
            const response = await api.put(`/admin/users/${id}`, userData);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la modification');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    archiveUser: async (id) => {
        try {
            const response = await api.put(`/admin/users/${id}/archive`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de l\'archivage');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    restoreUser: async (id) => {
        try {
            const response = await api.put(`/admin/users/${id}/restore`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la restauration');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    assignRole: async (id, role) => {
        try {
            const response = await api.put(`/admin/users/${id}/role`, { role });
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de l\'assignation du rôle');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    // Role-specific user fetching methods
    getRHUsers: async () => {
        try {
            const response = await api.get('/admin/users/rh');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des utilisateurs RH');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    getMedecinUsers: async () => {
        try {
            const response = await api.get('/admin/users/medecin');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des médecins');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    getCollaborateurUsers: async () => {
        try {
            const response = await api.get('/admin/users/collaborateur');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des collaborateurs');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    }
    
};