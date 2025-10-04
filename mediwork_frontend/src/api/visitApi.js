import api from '../config/api';

export const visitApi = {
    getMyVisits: async () => {
        try {
            const response = await api.get('/visits/my-visits');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des visites');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Confirme une visite (médecin)
     * @param {string} visitId - L'identifiant de la visite
     */
    confirmVisit: async (visitId) => {
        try {
            const response = await api.put(`/doctor/visits/${visitId}/confirm`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la confirmation de la visite');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Rejette une visite (médecin)
     * @param {string} visitId - L'identifiant de la visite
     */
    rejectVisit: async (visitId) => {
        try {
            const response = await api.put(`/doctor/visits/${visitId}/reject`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors du rejet de la visite');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Met à jour le statut d'une visite (démarrer, terminer, annuler)
     * @param {string} visitId - L'identifiant de la visite
     * @param {string} status - Le nouveau statut (ex: 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
     */
    updateVisitStatus: async (visitId, status) => {
        try {
            const response = await api.put(`/doctor/visits/${visitId}/status?status=${status}`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la mise à jour du statut');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Annule une visite
     * @param {string} visitId - L'identifiant de la visite
     */
    cancelVisit: async (visitId) => {
        try {
            const response = await api.put(`/visits/${visitId}/cancel`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de l\'annulation de la visite');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Récupère toutes les demandes de visites spontanées (RH)
     */
    getSpontaneousVisits: async () => {
        try {
            const response = await api.get('/spontaneous-visits');
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la récupération des demandes spontanées');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Met à jour une demande de visite spontanée
     * @param {string} visitId - L'identifiant de la visite spontanée
     * @param {Object} updateData - Les données à mettre à jour
     */
    updateSpontaneousVisit: async (visitId, updateData) => {
        try {
            const response = await api.put(`/spontaneous-visits/${visitId}`, updateData);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la mise à jour de la demande');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    },

    /**
     * Supprime une demande de visite spontanée
     * @param {string} visitId - L'identifiant de la visite spontanée
     */
    deleteSpontaneousVisit: async (visitId) => {
        try {
            const response = await api.delete(`/spontaneous-visits/${visitId}/cancel`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Erreur lors de la suppression de la demande');
            } else {
                throw new Error('Erreur de connexion');
            }
        }
    }
};