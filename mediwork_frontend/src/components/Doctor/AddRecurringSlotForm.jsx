import React, { useState } from "react";
import axios from "axios";

export default function AddRecurringSlotForm({ onSuccess }) {
  const [formData, setFormData] = useState({
    dayOfWeek: "MONDAY",
    startTime: "08:00",
    endTime: "16:00"
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const daysOfWeek = [
    { value: "MONDAY", label: "Lundi" },
    { value: "TUESDAY", label: "Mardi" },
    { value: "WEDNESDAY", label: "Mercredi" },
    { value: "THURSDAY", label: "Jeudi" },
    { value: "FRIDAY", label: "Vendredi" },
    { value: "SATURDAY", label: "Samedi" },
    { value: "SUNDAY", label: "Dimanche" }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    // Validation
    if (formData.startTime >= formData.endTime) {
      setError("L'heure de début doit être avant l'heure de fin");
      setLoading(false);
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "http://localhost:8081/api/recurring-slots",
        formData,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      console.log("Créneau récurrent créé:", response.data);
      
      // Réinitialiser le formulaire
      setFormData({
        dayOfWeek: "MONDAY",
        startTime: "08:00",
        endTime: "16:00"
      });
      
      // Notifier le parent du succès
      onSuccess();
      
      // Afficher un message de succès
      alert("Créneau récurrent créé avec succès !");
      
    } catch (error) {
      console.error("Erreur lors de la création du créneau récurrent:", error);
      
      if (error.response?.status === 400) {
        setError("Données invalides. Vérifiez vos informations.");
      } else if (error.response?.status === 409) {
        setError("Vous avez déjà un créneau récurrent pour ce jour de la semaine.");
      } else {
        setError(error.response?.data?.message || "Erreur lors de la création du créneau");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Effacer l'erreur quand l'utilisateur modifie le formulaire
    if (error) setError("");
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-md">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">
        Ajouter un Créneau Récurrent
      </h3>

      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {/* Jour de la semaine */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Jour de la semaine *
          </label>
          <select
            name="dayOfWeek"
            value={formData.dayOfWeek}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            required
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
            Heure de début *
          </label>
          <input
            type="time"
            name="startTime"
            value={formData.startTime}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            required
          />
        </div>

        {/* Heure de fin */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Heure de fin *
          </label>
          <input
            type="time"
            name="endTime"
            value={formData.endTime}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            required
          />
        </div>

        {/* Résumé de la configuration */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="font-medium text-blue-800 mb-2">Résumé de la configuration</h4>
          <p className="text-sm text-blue-700">
            Créneau récurrent tous les{" "}
            <strong>{daysOfWeek.find(d => d.value === formData.dayOfWeek)?.label}</strong>{" "}
            de <strong>{formData.startTime}</strong> à <strong>{formData.endTime}</strong>.
          </p>
          <p className="text-xs text-blue-600 mt-2">
            Ce créneau sera disponible chaque semaine pour la planification des rendez-vous.
          </p>
        </div>

        {/* Bouton de soumission */}
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors"
        >
          {loading ? (
            <span className="flex items-center justify-center">
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Création en cours...
            </span>
          ) : (
            "➕ Créer le créneau récurrent"
          )}
        </button>
      </form>

      {/* Informations supplémentaires */}
      <div className="mt-6 p-4 bg-gray-50 rounded-lg">
        <h4 className="font-medium text-gray-800 mb-2">ℹ️ Informations</h4>
        <ul className="text-sm text-gray-600 space-y-1">
          <li>• Un seul créneau récurrent par jour de la semaine</li>
          <li>• Les créneaux sont automatiquement disponibles chaque semaine</li>
          <li>• Vous pouvez modifier ou supprimer vos créneaux à tout moment</li>
          <li>• Les heures doivent être au format 24h (ex: 14:30)</li>
        </ul>
      </div>
    </div>
  );
}