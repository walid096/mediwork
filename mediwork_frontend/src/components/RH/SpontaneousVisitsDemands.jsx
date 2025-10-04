import React, { useEffect, useState } from 'react';
import { visitApi } from '../../api/visitApi';
import PageWrapper from '../Layout/PageWrapper';
import axios from 'axios';

export default function SpontaneousVisitsDemands() {
  const [demands, setDemands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Modal states
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedDemand, setSelectedDemand] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [scheduleForm, setScheduleForm] = useState({
    doctorId: '',
    visitType: 'SPONTANEOUS',
    startTime: '',
    collaboratorId: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    fetchSpontaneousVisits();
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get("http://localhost:8081/api/users/doctors", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDoctors(response.data);
    } catch (err) {
      console.error("Erreur chargement médecins:", err);
    }
  };

  const fetchSpontaneousVisits = async () => {
    try {
      setLoading(true);
      const data = await visitApi.getSpontaneousVisits();
      setDemands(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelDemand = async (demandId) => {
    const confirmed = window.confirm('Voulez-vous vraiment supprimer cette demande ?');
    if (!confirmed) return;
    try {
      setDeletingId(demandId);
      await visitApi.deleteSpontaneousVisit(demandId);
      await fetchSpontaneousVisits();
    } catch (err) {
      console.error('Erreur lors de la suppression:', err);
      setError(err.message || "Impossible de supprimer la demande");
    } finally {
      setDeletingId(null);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { bg: 'bg-yellow-100', text: 'text-yellow-800', label: 'En attente' },
      SCHEDULED: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'Planifiée' },
      CANCELLED: { bg: 'bg-red-100', text: 'text-red-800', label: 'Annulée' }
    };

    const config = statusConfig[status] || { bg: 'bg-gray-100', text: 'text-gray-800', label: status };
    
    return (
      <span className={`px-2 py-1 rounded-full text-md font-medium ${config.bg} ${config.text}`}>
        {config.label}
      </span>
    );
  };

  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '-';
    
    const date = new Date(dateTimeString);
    const dateStr = date.toLocaleDateString('fr-FR');
    const timeStr = date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    
    return `${dateStr} à ${timeStr}`;
  };

  const handleScheduleClick = (demand) => {
    setSelectedDemand(demand);
    setModalError(null); // Clear any previous modal errors
    
    // Pre-fill form with preferred date/time if available
    const preferredDateTime = demand.preferredDateTime 
      ? new Date(demand.preferredDateTime).toISOString().slice(0, 16)
      : '';
    
    // Calculate end time (1 hour after start time)
    const endDateTime = preferredDateTime 
      ? new Date(new Date(preferredDateTime).getTime() + 60 * 60 * 1000).toISOString().slice(0, 16)
      : '';
    
    setScheduleForm({
      doctorId: '',
      visitType: 'SPONTANEOUS',
      startTime: preferredDateTime,
      endTime: endDateTime,
      collaboratorId: demand.collaboratorId || ''
    });
    
    setShowScheduleModal(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setModalError(null); // Clear error when user makes changes
    setScheduleForm(prev => {
      const updated = { ...prev, [name]: value };
      
      // Auto-calculate end time when start time changes
      if (name === 'startTime' && value) {
        const startDate = new Date(value);
        const endDate = new Date(startDate.getTime() + 60 * 60 * 1000); // Add 1 hour
        updated.endTime = endDate.toISOString().slice(0, 16);
      }
      
      return updated;
    });
  };

  const handleScheduleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null); // Clear any previous errors
    
    try {
      const token = localStorage.getItem("token");
      await axios.post(
        `http://localhost:8081/api/spontaneous-visits/${selectedDemand.id}/confirm`,
        {
          collaboratorId: scheduleForm.collaboratorId || selectedDemand.collaboratorId,
          doctorId: scheduleForm.doctorId,
          visitType: scheduleForm.visitType,
          dateTime: scheduleForm.startTime,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      
      // Refresh the demands list
      await fetchSpontaneousVisits();
      
      // Close modal
      setShowScheduleModal(false);
      setSelectedDemand(null);
      
    } catch (err) {
      console.error("Erreur lors de la planification:", err);
      // Extract error message from response
      const errorMessage = err.response?.data?.message || "Erreur lors de la planification de la visite";
      setModalError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  const closeModal = () => {
    setShowScheduleModal(false);
    setSelectedDemand(null);
    setModalError(null); // Clear modal error when closing
    setScheduleForm({
      doctorId: '',
      visitType: 'SPONTANEOUS',
      startTime: '',
      endTime: '',
      collaboratorId: ''
    });
  };

  if (loading) {
    return (
      <PageWrapper title="Demandes de visites spontanées">
        <div className="flex justify-center items-center p-8">
          <div className="text-lg text-gray-600">Chargement des demandes...</div>
        </div>
      </PageWrapper>
    );
  }

  if (error) {
    return (
      <PageWrapper title="Demandes de visites spontanées">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      </PageWrapper>
    );
  }

  return (
    <PageWrapper title="Demandes de visites spontanées">
      <div className="p-5">
        {/* Header avec bouton de rafraîchissement */}
        <div className="flex justify-end mb-3">
          <button
            onClick={fetchSpontaneousVisits}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>Actualiser</span>
          </button>
        </div>

        {/* Liste des demandes */}
        {demands.length === 0 ? (
          <div className="text-center py-12 bg-gray-50 rounded-lg">
            <div className="text-gray-500">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p className="text-lg font-medium">Aucune demande de visite spontanée</p>
              <p className="text-sm mt-2">Les nouvelles demandes apparaîtront ici</p>
            </div>
          </div>
        ) : (
          <div className="grid gap-6">
            {demands.map((demand) => (
              <div
                key={demand.id}
                className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="space-y-4">
                  {/* En-tête avec collaborateur et statut */}
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800">
                        {demand.collaboratorName}
                      </h3>
                      <p className="text-sm text-gray-500">ID: #{demand.id}</p>
                    </div>
                    <div className="flex flex-col items-end space-y-2">
                      {getStatusBadge(demand.schedulingStatus)}
                      <p className="text-xs text-gray-500">
                        Crée le {formatDateTime(demand.createdAt)}
                      </p>
                    </div>
                  </div>

                  {/* Motif de la visite */}
                  <div className="bg-gray-50 rounded-lg p-4">
                    <h4 className="text-sm font-medium text-gray-700 mb-2">Motif de la visite</h4>
                    <p className="text-gray-800">{demand.reason}</p>
                  </div>

                  {/* Notes supplémentaires */}
                  {demand.additionalNotes && (
                    <div className="rounded-lg p-4">
                      <h4 className="text-sm font-medium text-gray-700 mb-2">Notes supplémentaires</h4>
                      <p className="text-gray-800">{demand.additionalNotes}</p>
                    </div>
                  )}

                  {/* Date préférée */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="rounded-lg p-4">
                      <h4 className="text-sm font-medium text-green-700 mb-2">Date préférée</h4>
                      <p className="text-green-800">
                        {demand.preferredDateTime 
                          ? formatDateTime(demand.preferredDateTime)
                          : 'Aucune préférence spécifiée'
                        }
                      </p>
                    </div>
                    
                    {/* Actions possibles selon le statut */}
                    <div className="flex items-center justify-end space-x-2">
                      {demand.schedulingStatus === 'PENDING' && (
                        <>
                          <button 
                            onClick={() => handleScheduleClick(demand)}
                            className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors text-sm"
                          >
                            Planifier
                          </button>
                          <button
                            onClick={() => handleCancelDemand(demand.id)}
                            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors text-sm disabled:opacity-60"
                            disabled={deletingId === demand.id}
                          >
                            {deletingId === demand.id ? 'Annulation...' : 'Annuler'}
                          </button>
                        </>
                      )}
                      {demand.schedulingStatus === 'NEEDS_RESCHEDULING' && (
                        <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm">
                          Reprogrammer
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal de planification */}
      {showScheduleModal && selectedDemand && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-semibold text-gray-800">
                Planifier la visite
              </h3>
              <button
                onClick={closeModal}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Info de la demande */}
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <h4 className="font-medium text-gray-800 mb-2">
                {selectedDemand.collaboratorName}
              </h4>
              <p className="text-sm text-gray-600 mb-2">
                <strong>Motif:</strong> {selectedDemand.reason}
              </p>
              {selectedDemand.preferredDateTime && (
                <p className="text-sm text-gray-600">
                  <strong>Date préférée:</strong> {formatDateTime(selectedDemand.preferredDateTime)}
                </p>
              )}
            </div>

            {/* Error display inside modal */}
            {modalError && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                {modalError}
              </div>
            )}

            {/* Formulaire */}
            <form onSubmit={handleScheduleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Médecin <span className="text-red-500">*</span>
                </label>
                <select
                  name="doctorId"
                  value={scheduleForm.doctorId}
                  onChange={handleFormChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                  disabled={submitting}
                >
                  <option value="">Sélectionner un médecin</option>
                  {doctors.map((doctor) => (
                    <option key={doctor.id} value={doctor.id}>
                      Dr. {doctor.firstName} {doctor.lastName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Type de visite
                </label>
                <select
                  name="visitType"
                  value={scheduleForm.visitType}
                  onChange={handleFormChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  disabled={submitting}
                >
                  <option value="SPONTANEOUS">Spontanée</option>
                  <option value="HIRING">Embauche</option>
                  <option value="PERIODIC">Périodique</option>
                  <option value="RETURN_TO_WORK">Retour au travail</option>
                  <option value="JOB_CHANGE">Changement de poste</option>
                  <option value="MEDICAL_FOLLOW_UP">Suivi médical</option>
                  <option value="EXCEPTIONAL_VISIT">Exceptionnelle</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Heure de début <span className="text-red-500">*</span>
                </label>
                <input
                  type="datetime-local"
                  name="startTime"
                  value={scheduleForm.startTime}
                  onChange={handleFormChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                  disabled={submitting}
                  min={new Date().toISOString().slice(0, 16)}
                />
              </div>

              {/* Boutons */}
              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors"
                  disabled={submitting}
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                  disabled={submitting || !scheduleForm.doctorId || !scheduleForm.startTime}
                >
                  {submitting ? 'Planification...' : 'Planifier'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </PageWrapper>
  );
}
