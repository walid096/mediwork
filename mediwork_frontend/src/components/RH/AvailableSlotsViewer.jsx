import React, { useEffect, useState } from "react";
import axios from "axios";
import VisitScheduler from "./VisitScheduler";

export default function AvailableSlotsViewer() {
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showScheduler, setShowScheduler] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);

  // ✅ FIXED: Time formatting function that handles comma issues
  const formatTime = (time) => {
    if (!time) return "";
    
    // Handle LocalTime objects from backend
    if (typeof time === 'string') {
      // If it's already a formatted string like "08:00:00"
      if (time.includes(':')) {
        const [hours, minutes] = time.split(':');
        return `${parseInt(hours)}:${minutes.padStart(2, '0')}`;
      }
      // If it's a raw number like "800" or "8,0" (comma issue)
      else if (time.includes(',')) {
        // Handle comma-separated format like "8,0" or "8,30"
        const [hours, minutes] = time.split(',');
        return `${parseInt(hours)}:${parseInt(minutes).toString().padStart(2, '0')}`;
      }
      else if (time.length === 3 || time.length === 4) {
        // Handle raw number format like "800" or "830"
        const hours = time.slice(0, -2);
        const minutes = time.slice(-2);
        return `${parseInt(hours)}:${minutes}`;
      }
    }
    
    // Handle LocalTime object with hour/minute properties
    if (time && typeof time === 'object') {
      if (time.hour !== undefined && time.minute !== undefined) {
        return `${time.hour}:${time.minute.toString().padStart(2, '0')}`;
      }
    }
    
    // Fallback: return as is
    return time?.toString() || '';
  };

  // Charger la liste des médecins
  useEffect(() => {
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
    fetchDoctors();
  }, []);

  // Charger les slots dispo du médecin sélectionné
  const fetchSlots = async (doctorId) => {
    setLoading(true);
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get(
        `http://localhost:8081/api/recurring-slots/doctor/${doctorId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      console.log("Slots disponibles:", response.data);
      setSlots(response.data);
    } catch (err) {
      console.error("Erreur chargement slots:", err);
      setSlots([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle Planifier button click
  const handlePlanifierClick = (slot) => {
    console.log("Opening modal with slot:", slot);
    console.log("Selected doctor ID:", selectedDoctor);
    setSelectedSlot(slot);
    setShowScheduler(true);
  };

  return (
    <div className="w-full max-w-2xl mx-auto px-6 py-8 bg-white rounded-2xl shadow-lg border border-gray-100 mt-10">
      <h2 className="text-2xl font-bold mb-8 text-blue-700 flex items-center gap-2">
        <span role="img" aria-label="calendar">📅</span> Créneaux disponibles
      </h2>

      {/* Sélection du médecin */}
      <div className="mb-8 flex flex-col sm:flex-row sm:items-center gap-4">
        <label className="block font-semibold text-gray-700 min-w-[150px]">Choisir un médecin :</label>
        <select
          onChange={(e) => {
            const doctorId = e.target.value;
            setSelectedDoctor(doctorId);
            if (doctorId) {
              fetchSlots(doctorId);
            } else {
              setSlots([]);
            }
          }}
          className="border border-gray-300 rounded-lg px-4 py-2 w-full sm:w-64 bg-gray-50 text-gray-800 focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none transition"
        >
          <option value="">-- Sélectionner --</option>
          {doctors.map((doc) => (
            <option key={doc.id} value={doc.id}>
              {doc.firstName} {doc.lastName}
            </option>
          ))}
        </select>
      </div>

      {/* Liste des slots */}
      <div className="min-h-[80px]">
        {loading ? (
          <div className="flex items-center gap-2 text-gray-500 animate-pulse">
            <svg className="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"></path>
            </svg>
            Chargement des créneaux...
          </div>
        ) : slots.length === 0 ? (
          <p className="text-gray-500">Aucun créneau disponible.</p>
        ) : (
          <ul className="space-y-4">
            {slots.map((slot) => (
              <li
                key={slot.id}
                className="p-4 border border-gray-200 rounded-xl flex flex-col sm:flex-row sm:justify-between sm:items-center bg-gray-50 shadow-sm"
              >
                <span className="text-gray-800 font-medium">
                  {/* ✅ FIX: Use formatted times */}
                  {slot.dayOfWeek} de {formatTime(slot.startTime)} à {formatTime(slot.endTime)}
                </span>
                <button
                  onClick={() => handlePlanifierClick(slot)}
                  className="mt-3 sm:mt-0 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg font-semibold shadow transition"
                >
                  Planifier
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Modal Planification - FIXED PROPS */}
      {showScheduler && selectedSlot && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white p-0 rounded-2xl shadow-2xl w-full max-w-lg border border-gray-100">
            <VisitScheduler
              onClose={() => setShowScheduler(false)}
              selectedSlot={selectedSlot}
            />
          </div>
        </div>
      )}
    </div>
  );
}