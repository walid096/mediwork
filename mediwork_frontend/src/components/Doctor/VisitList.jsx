import React, { useEffect, useState } from 'react';
import { visitApi } from '../../api/visitApi';
// ...existing code...

const VisitList = () => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState({});

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

  // Fonction pour confirmer une visite
  const handleConfirmVisit = async (visitId) => {
    setActionLoading((prev) => ({ ...prev, [visitId]: true }));
    try {
      await visitApi.confirmVisit(visitId);
      // Recharger la liste après confirmation
      await fetchVisits();
    } catch (err) {
      setError('Erreur lors de la confirmation de la visite');
    } finally {
      setActionLoading((prev) => ({ ...prev, [visitId]: false }));
    }
  };

  // Fonction pour rejeter une visite
  const handleRejectVisit = async (visitId) => {
    setActionLoading((prev) => ({ ...prev, [visitId]: true }));
    try {
      await visitApi.rejectVisit(visitId);
      // Recharger la liste après rejet
      await fetchVisits();
    } catch (err) {
      setError('Erreur lors du rejet de la visite');
    } finally {
      setActionLoading((prev) => ({ ...prev, [visitId]: false }));
    }
  };

  // Fonction pour mettre à jour le statut
  const handleStatusChange = async (visitId, newStatus) => {
    setActionLoading((prev) => ({ ...prev, [visitId]: true }));
    try {
      await visitApi.updateVisitStatus(visitId, newStatus);
      // Recharger la liste après mise à jour
      await fetchVisits();
    } catch (err) {
      setError('Erreur lors de la mise à jour du statut');
    } finally {
      setActionLoading((prev) => ({ ...prev, [visitId]: false }));
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

  // Rendre les boutons d'action selon le statut avec Tailwind
  const renderActionButtons = (visit) => {
    const baseBtn =
      'inline-flex items-center px-2 py-1 border text-xs font-medium rounded transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none';
    switch (visit.status) {
      case 'PENDING_DOCTOR_CONFIRMATION':
        return (
          <div className="flex gap-2">
            <button
              className={`${baseBtn} border-green-600 text-green-700 bg-green-50 hover:bg-green-100`}
              disabled={actionLoading[visit.id]}
              onClick={() => handleConfirmVisit(visit.id)}
              title="Confirmer"
            >
              ✔ Confirmer
            </button>
            <button
              className={`${baseBtn} border-red-600 text-red-700 bg-red-50 hover:bg-red-100`}
              disabled={actionLoading[visit.id]}
              onClick={() => handleRejectVisit(visit.id)}
              title="Rejeter"
            >
              ✖ Rejeter
            </button>
          </div>
        );
      case 'SCHEDULED':
        return (
          <div className="flex gap-2">
            <button
              className={`${baseBtn} border-blue-600 text-blue-700 bg-blue-50 hover:bg-blue-100`}
              disabled={actionLoading[visit.id]}
              onClick={() => handleStatusChange(visit.id, 'IN_PROGRESS')}
              title="Démarrer"
            >
              ▶ Démarrer
            </button>
            <button
              className={`${baseBtn} border-red-600 text-red-700 bg-red-50 hover:bg-red-100`}
              disabled={actionLoading[visit.id]}
              onClick={() => handleStatusChange(visit.id, 'CANCELLED')}
              title="Annuler"
            >
              ✖ Annuler
            </button>
          </div>
        );
      case 'IN_PROGRESS':
        return (
          <button
            className={`${baseBtn} border-green-600 text-green-700 bg-green-50 hover:bg-green-100`}
            disabled={actionLoading[visit.id]}
            onClick={() => handleStatusChange(visit.id, 'COMPLETED')}
            title="Terminer"
          >
            ✔ Terminer
          </button>
        );
      default:
        return <span className="text-xs text-gray-400">Aucune action</span>;
    }
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
        Aucune visite trouvée.
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto mt-8 px-2 sm:px-4">
      <div className="p-4 bg-gray-50 border-b rounded-t-lg flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Mes visites</h1>
        <span className="text-sm text-gray-500 mt-2 sm:mt-0">{visits.length} visite(s) trouvée(s)</span>
      </div>
      <div className="overflow-x-auto bg-white rounded-b-lg shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Date</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Heure</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Patient</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Type de visite</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Statut</th>
              <th className="px-4 py-2 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Actions</th>
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
                      <span className="font-medium text-gray-800">{visit.collaborator?.firstName} {visit.collaborator?.lastName}</span>
                      <span className="text-xs text-gray-400">{visit.collaborator?.matricule}</span>
                    </div>
                  </td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    <span className="px-2 py-1 border rounded-full text-xs font-semibold bg-gray-50 text-gray-700 border-gray-200">
                      {formatVisitType(visit.visitType)}
                    </span>
                  </td>
                  <td className="px-4 py-2 whitespace-nowrap">{getStatusChip(visit.status)}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{renderActionButtons(visit)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default VisitList;