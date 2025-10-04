
import React, { useEffect, useState } from "react";
import axios from "axios";
import { visitApi } from "../../api/visitApi";


export default function CollabVisits() {
  const [demandes, setDemandes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  // Nouveaux états pour la création
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState({
    reason: "",
    additionalNotes: "",
    preferredDateTime: ""
  });
  // États pour l'édition
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState({
    reason: "",
    additionalNotes: "",
    preferredDateTime: ""
  });
  // État pour la confirmation de suppression
  const [deletingId, setDeletingId] = useState(null);
  // Gérer les changements du formulaire de création
  const handleCreateChange = (e) => {
    const { name, value } = e.target;
    setCreateForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Gérer les changements du formulaire d'édition
  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Commencer l'édition d'une demande
  const startEditing = (demande) => {
    setEditingId(demande.id);
    setEditForm({
      reason: demande.reason,
      additionalNotes: demande.additionalNotes || "",
      preferredDateTime: new Date(demande.preferredDateTime).toISOString().slice(0, 16)
    });
  };

  // Annuler l'édition
  const cancelEditing = () => {
    setEditingId(null);
    setEditForm({ reason: "", additionalNotes: "", preferredDateTime: "" });
  };

  // Soumettre la nouvelle demande
  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      await axios.post(
        "http://localhost:8081/api/spontaneous-visits",
        createForm,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      fetchDemandes();
      setShowCreateForm(false);
      setCreateForm({ reason: "", additionalNotes: "", preferredDateTime: "" });
      alert("Demande Crée avec succès");
    } catch (error) {
      console.error("Erreur lors de la création:", error);
      alert("Impossible de créer la demande");
    }
  };

  // Soumettre les modifications d'une demande
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await visitApi.updateSpontaneousVisit(editingId, editForm);
      fetchDemandes();
      cancelEditing();
      alert("Demande mise à jour avec succès");
    } catch (error) {
      console.error("Erreur lors de la mise à jour:", error);
      alert("Impossible de mettre à jour la demande");
    }
  };

  // Supprimer une demande
  const handleDelete = async (id) => {
    if (window.confirm("Êtes-vous sûr de vouloir supprimer cette demande ?")) {
      try {
        await visitApi.deleteSpontaneousVisit(id);
        fetchDemandes();
        alert("Demande supprimée avec succès");
      } catch (error) {
        console.error("Erreur lors de la suppression:", error);
        alert("Impossible de supprimer la demande");
      }
    }
  };

  const daysOfWeek = [
    { value: "MONDAY", label: "Lundi" },
    { value: "TUESDAY", label: "Mardi" },
    { value: "WEDNESDAY", label: "Mercredi" },
    { value: "THURSDAY", label: "Jeudi" },
    { value: "FRIDAY", label: "Vendredi" },
    { value: "SATURDAY", label: "Samedi" },
    { value: "SUNDAY", label: "Dimanche" }
  ];

  // Charger les demandes spontanées
  const fetchDemandes = async () => {
    try {
        
      setLoading(true);
      const token = localStorage.getItem("token");
      const response = await axios.get(
        "http://localhost:8081/api/spontaneous-visits/my-requests",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      console.log("Données reçues:", response.data);
      setDemandes(response.data);
      setError("");
    } catch (error) {
      console.error("Erreur lors du chargement des demandes spontanées:", error);
      setError("Impossible de charger les demandes spontanées");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDemandes();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="text-lg">Chargement des demandes spontanées...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6 sm:px-8 px-3">
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-semibold text-gray-800">
          Mes Demandes de Visite Spontanée
        </h3>
        <div className="flex gap-2">
          <button
            onClick={fetchDemandes}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            🔄 Actualiser
          </button>
          <button
            onClick={() => setShowCreateForm((v) => !v)}
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
          >
            ➕ Nouvelle demande
          </button>
        </div>
      </div>

      {/* Formulaire de création */}
      {showCreateForm && (
        <form onSubmit={handleCreateSubmit} className="bg-white border border-gray-200 rounded-lg p-6 shadow space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Motif de la visite <span className="text-red-500">*</span></label>
              <input
                type="text"
                name="reason"
                value={createForm.reason}
                onChange={handleCreateChange}
                className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Consultation médicale urgente"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Date et heure souhaitées <span className="text-red-500">*</span></label>
              <input
                type="datetime-local"
                name="preferredDateTime"
                value={createForm.preferredDateTime}
                onChange={handleCreateChange}
                className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                required
                min={new Date().toISOString().slice(0, 16)}
              />
            </div>
          </div>
          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">Notes additionnelles</label>
            <textarea
              name="additionalNotes"
              value={createForm.additionalNotes}
              onChange={handleCreateChange}
              className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
              placeholder="Ajoutez des précisions si besoin..."
              rows={2}
            />
          </div>
          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setShowCreateForm(false)}
              className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
            >
              Annuler
            </button>
            <button
              type="submit"
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Créer
            </button>
          </div>
        </form>
      )}

      {demandes.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p className="text-lg">Aucune demande de visite spontanée</p>
          <p className="text-sm mt-2">
            Créez une demande pour solliciter une visite spontanée auprès d’un médecin
          </p>
        </div>
      ) : (
        <div className="grid gap-4">
          {demandes.map((demande) => (
            <div
              key={demande.id}
              className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm hover:shadow-md transition-shadow"
            >
              {editingId === demande.id ? (
                // Formulaire d'édition
                <form onSubmit={handleEditSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Motif de la visite <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        name="reason"
                        value={editForm.reason}
                        onChange={handleEditChange}
                        className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                        placeholder="Ex: Consultation médicale urgente"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Date et heure souhaitées <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="datetime-local"
                        name="preferredDateTime"
                        value={editForm.preferredDateTime}
                        onChange={handleEditChange}
                        className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                        required
                        min={new Date().toISOString().slice(0, 16)}
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Notes additionnelles</label>
                    <textarea
                      name="additionalNotes"
                      value={editForm.additionalNotes}
                      onChange={handleEditChange}
                      className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                      placeholder="Ajoutez des précisions si besoin..."
                      rows={2}
                    />
                  </div>
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={cancelEditing}
                      className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
                    >
                      Annuler
                    </button>
                    <button
                      type="submit"
                      className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                      Sauvegarder
                    </button>
                  </div>
                </form>
              ) : (
                // Affichage normal
                <div className="space-y-4">
                  {/* En-tête avec nom du collaborateur et statut */}
                  <div className="flex justify-between items-start">
                    <div>
                      <h4 className="text-lg font-semibold text-gray-800">
                        {demande.collaboratorName}
                      </h4>
                      <div className="flex items-center space-x-2 mt-1">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                          demande.schedulingStatus === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                          demande.schedulingStatus === 'APPROVED' ? 'bg-green-100 text-green-800' :
                          demande.schedulingStatus === 'REJECTED' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {demande.schedulingStatus === 'PENDING' ? 'En attente' :
                           demande.schedulingStatus === 'APPROVED' ? 'Approuvée' :
                           demande.schedulingStatus === 'REJECTED' ? 'Rejetée' :
                           demande.schedulingStatus}
                        </span>
                      </div>
                    </div>
                    {/* Boutons d'action - seulement pour les demandes en attente */}
                    {demande.schedulingStatus === 'PENDING' && (
                      <div className="flex space-x-2">
                        <button
                          onClick={() => startEditing(demande)}
                          className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600"
                          title="Modifier la demande"
                        >
                          ✏️ Modifier
                        </button>
                        <button
                          onClick={() => handleDelete(demande.id)}
                          className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                          title="Supprimer la demande"
                        >
                          🗑️ Supprimer
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Motif de la visite */}
                  <div>
                    <h5 className="text-sm font-medium text-gray-700 mb-1">Motif de la visite</h5>
                    <p className="text-gray-800">{demande.reason}</p>
                  </div>

                  {/* Notes additionnelles */}
                  {demande.additionalNotes && (
                    <div>
                      <h5 className="text-sm font-medium text-gray-700 mb-1">Notes additionnelles</h5>
                      <p className="text-gray-800">{demande.additionalNotes}</p>
                    </div>
                  )}

                  {/* Dates */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <h5 className="text-sm font-medium text-gray-700 mb-1">Date souhaitée</h5>
                      <p className="text-gray-800">
                        {new Date(demande.preferredDateTime).toLocaleDateString('fr-FR', {
                          weekday: 'long',
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </p>
                      <p className="text-sm text-gray-600">
                        à {new Date(demande.preferredDateTime).toLocaleTimeString('fr-FR', {
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </p>
                    </div>
                    <div>
                      <h5 className="text-sm font-medium text-gray-700 mb-1">Date de création</h5>
                      <p className="text-gray-800">
                        {new Date(demande.createdAt).toLocaleDateString('fr-FR')}
                      </p>
                      <p className="text-sm text-gray-600">
                        {new Date(demande.createdAt).toLocaleTimeString('fr-FR', {
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Statistiques */}
      {demandes.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="font-medium text-blue-800 mb-2">Résumé</h4>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span className="text-blue-600 font-medium">Total :</span> {demandes.length} demande{demandes.length > 1 ? 's' : ''}
            </div>
            <div>
              <span className="text-blue-600 font-medium">En attente :</span> {demandes.filter(d => d.schedulingStatus === 'PENDING').length}
            </div>
            <div>
              <span className="text-blue-600 font-medium">Approuvées :</span> {demandes.filter(d => d.schedulingStatus === 'APPROVED').length}
            </div>
            <div>
              <span className="text-blue-600 font-medium">Rejetées :</span> {demandes.filter(d => d.schedulingStatus === 'REJECTED').length}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}