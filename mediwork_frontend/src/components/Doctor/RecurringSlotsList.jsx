import React, { useEffect, useState } from "react";
import axios from "axios";

export default function RecurringSlotsList() {
  const [recurringSlots, setRecurringSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingSlot, setEditingSlot] = useState(null);
  const [editForm, setEditForm] = useState({
    dayOfWeek: "",
    startTime: "",
    endTime: ""
  });

  const daysOfWeek = [
    { value: "MONDAY", label: "Lundi" },
    { value: "TUESDAY", label: "Mardi" },
    { value: "WEDNESDAY", label: "Mercredi" },
    { value: "THURSDAY", label: "Jeudi" },
    { value: "FRIDAY", label: "Vendredi" },
    { value: "SATURDAY", label: "Samedi" },
    { value: "SUNDAY", label: "Dimanche" }
  ];

  // Charger les créneaux récurrents
  const fetchRecurringSlots = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      const response = await axios.get(
        "http://localhost:8081/api/recurring-slots/my-recurring-slots",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setRecurringSlots(response.data);
      setError("");
    } catch (error) {
      console.error("Erreur lors du chargement des créneaux récurrents:", error);
      setError("Impossible de charger les créneaux récurrents");
    } finally {
      setLoading(false);
    }
  };

  // Supprimer un créneau récurrent
  const handleDelete = async (slotId) => {
    if (!window.confirm("Voulez-vous vraiment supprimer ce créneau récurrent ?")) return;
    
    try {
      const token = localStorage.getItem("token");
      await axios.delete(`http://localhost:8081/api/recurring-slots/${slotId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
      // Recharger la liste
      fetchRecurringSlots();
      alert("Créneau récurrent supprimé avec succès");
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
      alert("Impossible de supprimer ce créneau récurrent");
    }
  };

  // Commencer l'édition
  const handleEdit = (slot) => {
    setEditingSlot(slot);
    setEditForm({
      dayOfWeek: slot.dayOfWeek,
      startTime: slot.startTime,
      endTime: slot.endTime
    });
  };

  // Sauvegarder les modifications
  const handleSaveEdit = async () => {
    try {
      const token = localStorage.getItem("token");
      await axios.put(
        `http://localhost:8081/api/recurring-slots/${editingSlot.id}`,
        editForm,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      
      // Recharger la liste et annuler l'édition
      fetchRecurringSlots();
      setEditingSlot(null);
      setEditForm({ dayOfWeek: "", startTime: "", endTime: "" });
      alert("Créneau récurrent modifié avec succès");
    } catch (error) {
      console.error("Erreur lors de la modification:", error);
      alert("Impossible de modifier ce créneau récurrent");
    }
  };

  // Annuler l'édition
  const handleCancelEdit = () => {
    setEditingSlot(null);
    setEditForm({ dayOfWeek: "", startTime: "", endTime: "" });
  };

  // Gérer les changements du formulaire d'édition
  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Fonction pour formater les jours
  const formatDays = (slots) => {
    return slots.map(slot => daysOfWeek.find(d => d.value === slot.dayOfWeek)?.label).join(", ");
  };

  // Fonction pour formater la plage horaire
  const formatTimeRange = (slots) => {
    if (slots.length === 0) return "Aucune";
    
    const startTimes = slots.map(s => s.startTime).sort();
    const endTimes = slots.map(s => s.endTime).sort();
    
    return `${startTimes[0]} - ${endTimes[endTimes.length - 1]}`;
  };

  // Fonction pour formater la dernière modification
  const formatLastModified = (slots) => {
    if (slots.length === 0) return "Aucune";
    
    const dates = slots.map(s => s.updatedAt || s.createdAt);
    const latestDate = new Date(Math.max(...dates.map(d => new Date(d))));
    
    return latestDate.toLocaleDateString('fr-FR');
  };

  useEffect(() => {
    fetchRecurringSlots();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="text-lg">Chargement des créneaux récurrents...</div>
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
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-semibold text-gray-800">
          Mes Créneaux Récurrents
        </h3>
        <button
          onClick={fetchRecurringSlots}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          🔄 Actualiser
        </button>
      </div>

      {recurringSlots.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p className="text-lg">Aucun créneau récurrent défini</p>
          <p className="text-sm mt-2">
            Créez des créneaux récurrents pour définir votre disponibilité hebdomadaire
          </p>
        </div>
      ) : (
        <div className="grid gap-4">
          {recurringSlots.map((slot) => (
            <div
              key={slot.id}
              className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm hover:shadow-md transition-shadow"
            >
              {editingSlot?.id === slot.id ? (
                // Mode édition
                <div className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {/* Jour de la semaine */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Jour
                      </label>
                      <select
                        name="dayOfWeek"
                        value={editForm.dayOfWeek}
                        onChange={handleEditChange}
                        className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                      >
                        {daysOfWeek.map(day => (
                          <option key={day.value} value={day.value}>
                            {day.label}
                          </option>
                        ))}
                      </select>
                    </div>

                    {/* Heure de début */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Heure début
                      </label>
                      <input
                        type="time"
                        name="startTime"
                        value={editForm.startTime}
                        onChange={handleEditChange}
                        className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                      />
                    </div>

                    {/* Heure de fin */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Heure fin
                      </label>
                      <input
                        type="time"
                        name="endTime"
                        value={editForm.endTime}
                        onChange={handleEditChange}
                        className="w-full p-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </div>

                  {/* Boutons d'action */}
                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={handleCancelEdit}
                      className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
                    >
                      Annuler
                    </button>
                    <button
                      onClick={handleSaveEdit}
                      className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                    >
                      Sauvegarder
                    </button>
                  </div>
                </div>
              ) : (
                // Mode affichage
                <div className="flex justify-between items-center">
                  <div className="space-y-2">
                    <div className="flex items-center space-x-4">
                      <span className="text-lg font-medium text-gray-800">
                        {daysOfWeek.find(d => d.value === slot.dayOfWeek)?.label}
                      </span>
                      <span className="text-gray-500">
                        {slot.startTime} - {slot.endTime}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500">
                      Créé le {new Date(slot.createdAt).toLocaleDateString('fr-FR')}
                      {slot.updatedAt && (
                        <span className="ml-2">
                          • Modifié le {new Date(slot.updatedAt).toLocaleDateString('fr-FR')}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Boutons d'action */}
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleEdit(slot)}
                      className="bg-yellow-500 text-white px-3 py-1 rounded text-sm hover:bg-yellow-600"
                    >
                      ✏️ Modifier
                    </button>
                    <button
                      onClick={() => handleDelete(slot.id)}
                      className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                    >
                      🗑️ Supprimer
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Statistiques */}
      {recurringSlots.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="font-medium text-blue-800 mb-2">Résumé</h4>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span className="text-blue-600 font-medium">Total :</span> {recurringSlots.length} créneau{recurringSlots.length > 1 ? 'x' : ''}
            </div>
            <div>
              <span className="text-blue-600 font-medium">Jours :</span> {formatDays(recurringSlots)}
            </div>
            <div>
              <span className="text-blue-600 font-medium">Plage :</span> {formatTimeRange(recurringSlots)}
            </div>
            <div>
              <span className="text-blue-600 font-medium">Dernière modif :</span> {formatLastModified(recurringSlots)}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}