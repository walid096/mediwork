import React,  { useState } from "react";
import RecurringSlotsList from "./RecurringSlotsList";
import AddRecurringSlotForm from "./AddRecurringSlotForm";

export default function RecurringSlotsManager() {
  const [activeTab, setActiveTab] = useState("list"); // "list" ou "add"
  const [refreshKey, setRefreshKey] = useState(0);

  const handleSuccess = () => {
    // Forcer le rafraîchissement de la liste
    setRefreshKey(prev => prev + 1);
    // Retourner à l'onglet liste
    setActiveTab("list");
  };

  return (
    <div className="space-y-6 sm:px-8 px-3">
      {/* En-tête avec onglets */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab("list")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "list"
                ? "border-blue-500 text-blue-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            }`}
          >
            📋 Mes Créneaux Récurrents
          </button>
          <button
            onClick={() => setActiveTab("add")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "add"
                ? "border-blue-500 text-blue-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            }`}
          >
            ➕ Ajouter un Créneau
          </button>
        </nav>
      </div>

      {/* Contenu des onglets */}
      <div className="min-h-[400px]">
        {activeTab === "list" ? (
          <RecurringSlotsList key={refreshKey} />
        ) : (
          <AddRecurringSlotForm onSuccess={handleSuccess} />
        )}
      </div>

      {/* Bouton de retour rapide */}
      {activeTab === "add" && (
        <div className="text-center">
          <button
            onClick={() => setActiveTab("list")}
            className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
          >
            ← Retour à la liste
          </button>
        </div>
      )}
    </div>
  );
}