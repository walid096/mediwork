import React, { useEffect, useState } from 'react';
import { visitApi } from '../../api/visitApi';

const CollabAssignedVisits = () => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchVisits();
  }, []);

  const fetchVisits = async () => {
    try {
      const data = await visitApi.getMyVisits();
      setVisits(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Fonction pour formater le statut avec des couleurs Tailwind
  const getStatusChip = (status) => {
    const statusConfig = {
      'PENDING_DOCTOR_CONFIRMATION': {
        label: 'En attente de confirmation',
        className: 'bg-yellow-100 text-yellow-800 border-yellow-300'
      },
      'SCHEDULED': {
        label: 'Planifiée',
        className: 'bg-blue-100 text-blue-800 border-blue-300'
      },
      'IN_PROGRESS': {
        label: 'En cours',
        className: 'bg-indigo-100 text-indigo-800 border-indigo-300'
      },
      'COMPLETED': {
        label: 'Terminée',
        className: 'bg-green-100 text-green-800 border-green-300'
      },
      'CANCELLED': {
        label: 'Annulée',
        className: 'bg-red-100 text-red-800 border-red-300'
      }
    };
    const config = statusConfig[status] || { label: status, className: 'bg-gray-100 text-gray-800 border-gray-300' };
    return (
      <span className={`px-2 py-1 rounded-full border text-xs font-semibold ${config.className}`}>
        {config.label}
      </span>
    );
  };

  // Fonction pour formater le type de visite
  const formatVisitType = (visitType) => {
    const typeLabels = {
      'HIRING': 'Embauche',
      'PERIODIC': 'Périodique',
      'RETURN_TO_WORK': 'Retour au travail',
      'PRE_RETURN': 'Pré-retour',
      'JOB_CHANGE': 'Changement de poste',
      'SPONTANEOUS': 'Spontanée',
      'MEDICAL_FOLLOW_UP': 'Suivi médical',
      'EXCEPTIONAL_VISIT': 'Exceptionnelle'
    };
    return typeLabels[visitType] || visitType;
  };

  // Fonction pour formater la date et l'heure
  const formatDateTime = (startTime, endTime) => {
    if (!startTime) return { date: '-', time: '-' };
    
    const start = new Date(startTime);
    const end = endTime ? new Date(endTime) : null;
    
    const date = start.toLocaleDateString('fr-FR');
    const startTimeStr = start.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    const endTimeStr = end ? end.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) : null;
    
    return {
      date: date,
      time: endTimeStr ? `${startTimeStr} - ${endTimeStr}` : startTimeStr
    };
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <svg className="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"></path>
        </svg>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-md mx-auto mt-8">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      </div>
    );
  }

  if (!visits || visits.length === 0) {
    return (
      <div className="text-center mt-8 text-lg text-gray-500 font-semibold">
        Aucune visite assignée trouvée.
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto mt-8 px-2 sm:px-4">
      <div className="p-4 bg-gray-50 border-b rounded-t-lg flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Mes Visites Assignées</h1>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">{visits.length} visite(s) trouvée(s)</span>
          <button
            onClick={fetchVisits}
            className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600 transition-colors"
            title="Actualiser"
          >
            🔄 Actualiser
          </button>
        </div>
      </div>
      <div className="overflow-x-auto bg-white rounded-b-lg shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Date</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Heure</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Médecin</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Type de visite</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Statut</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Créée par</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-100">
            {visits.map((visit) => {
              const dateTime = formatDateTime(visit.slot?.startTime, visit.slot?.endTime);
              return (
                <tr key={visit.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-2 whitespace-nowrap">{dateTime.date}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{dateTime.time}</td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    <div className="flex flex-col">
                      <span className="font-medium text-gray-800">Dr. {visit.doctor?.firstName} {visit.doctor?.lastName}</span>
                      <span className="text-xs text-gray-400">{visit.doctor?.email}</span>
                    </div>
                  </td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    <span className="px-2 py-1 border rounded-full text-xs font-semibold bg-gray-50 text-gray-700 border-gray-200">
                      {formatVisitType(visit.visitType)}
                    </span>
                  </td>
                  <td className="px-4 py-2 whitespace-nowrap">{getStatusChip(visit.status)}</td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    <div className="flex flex-col">
                      <span className="text-sm text-gray-800">{visit.createdBy?.firstName} {visit.createdBy?.lastName}</span>
                      <span className="text-xs text-gray-400">RH</span>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default CollabAssignedVisits;