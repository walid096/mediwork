import React, { useState, useEffect } from "react";
import axios from "axios";
import DateScroller from "./DateScroller"; // ← ADDED IMPORT

// Helper function to get next occurrence of a day of week
const getNextOccurrence = (dayOfWeek) => {
  const today = new Date();
  const dayMap = {
    MONDAY: 1,
    TUESDAY: 2,
    WEDNESDAY: 3,
    THURSDAY: 4,
    FRIDAY: 5,
    SATURDAY: 6,
    SUNDAY: 0,
  };

  const targetDay = dayMap[dayOfWeek];
  let nextDate = new Date(today);
  const currentDay = today.getDay();

  // Calculate days until next occurrence
  let daysUntilNext = targetDay - currentDay;
  if (daysUntilNext <= 0) {
    daysUntilNext += 7; // Next week
  }

  nextDate.setDate(today.getDate() + daysUntilNext);
  return nextDate.toISOString().split("T")[0]; // Return YYYY-MM-DD
};

// ✅ Generate all available time slots from recurring slot
const generateAvailableTimeSlots = (recurringSlot) => {
  const slots = [];
  const slotDuration = 60; // 1 hour in minutes

  // Parse recurring slot times
  const [startHour, startMinute] = recurringSlot.startTime.split(":").map(Number);
  const [endHour, endMinute] = recurringSlot.endTime.split(":").map(Number); // ✅ FIXED

  // Convert to minutes
  const startMinutes = startHour * 60 + startMinute;
  const endMinutes = endHour * 60 + endMinute;

  // Generate all possible 1-hour slots
  for (let time = startMinutes; time + slotDuration <= endMinutes; time += slotDuration) {
    const slotStartHour = Math.floor(time / 60);
    const slotStartMinute = time % 60;
    const slotEndHour = Math.floor((time + slotDuration) / 60);
    const slotEndMinute = (time + slotDuration) % 60;

    slots.push({
      startTime: `${slotStartHour.toString().padStart(2, "0")}:${slotStartMinute
        .toString()
        .padStart(2, "0")}`,
      endTime: `${slotEndHour.toString().padStart(2, "0")}:${slotEndMinute
        .toString()
        .padStart(2, "0")}`,
      startMinutes: time,
      isAvailable: true, // Will be updated after conflict checking
    });
  }

  return slots;
};

// ✅ Check conflicts for time slots
const checkSlotConflicts = async (timeSlots, doctorId, date) => {
  try {
    const token = localStorage.getItem("token");
    const response = await axios.get(
      `http://localhost:8081/api/visits/doctor/${doctorId}/date/${date}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    );

    const existingBookings = response.data || [];

    // Mark slots as unavailable if they conflict
    return timeSlots.map((slot) => {
      const slotStart = `${date}T${slot.startTime}:00`;
      const slotEnd = `${date}T${slot.endTime}:00`;

      const slotStartDate = new Date(slotStart);
      const slotEndDate = new Date(slotEnd);

      const hasConflict = existingBookings.some((booking) => {
        const bookingStart = new Date(booking.startTime);
        const bookingEnd = new Date(booking.endTime);

        // ✅ Simplified overlap detection
        return slotStartDate < bookingEnd && slotEndDate > bookingStart;
      });

      return {
        ...slot,
        isAvailable: !hasConflict,
      };
    });
  } catch (error) {
    console.error("Error checking conflicts:", error);
    // Return all slots as available if conflict checking fails
    return timeSlots.map((slot) => ({ ...slot, isAvailable: true }));
  }
};

export default function VisitScheduler({ onClose, selectedSlot }) {
  const [visitData, setVisitData] = useState({
    collaboratorId: "",
    doctorId: "",
    visitType: "HIRING",
    startTime: "",
    endTime: "",
  });
  const [collaborators, setCollaborators] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [availableTimeSlots, setAvailableTimeSlots] = useState([]); // ✅
  const [selectedTimeSlot, setSelectedTimeSlot] = useState(null); // ✅
  const [slotLoading, setSlotLoading] = useState(false); // ✅
  const [selectedDate, setSelectedDate] = useState(null); // ← ADDED DATE STATE

  // ✅ Generate time slots when slot is selected OR date changes
  useEffect(() => {
    const generateSlots = async () => {
      if (selectedSlot) {
        setSlotLoading(true);
        try {
          const timeSlots = generateAvailableTimeSlots(selectedSlot);
          
          // Use selected date OR fall back to next occurrence
          const targetDate = selectedDate || getNextOccurrence(selectedSlot.dayOfWeek);
          
          const slotsWithConflicts = await checkSlotConflicts(
            timeSlots,
            selectedSlot.doctor.id,
            targetDate
          );

          setAvailableTimeSlots(slotsWithConflicts);
          setVisitData((prev) => ({
            ...prev,
            doctorId: selectedSlot.doctor.id,
          }));
        } catch (error) {
          console.error("Error generating time slots:", error);
          setMessage("❌ Erreur lors de la génération des créneaux disponibles");
        } finally {
          setSlotLoading(false);
        }
      }
    };

    generateSlots();
  }, [selectedSlot, selectedDate]); // ← ADDED selectedDate to dependency array

  // ✅ Handle time slot selection
  const handleTimeSlotSelect = (timeSlot) => {
    if (!timeSlot.isAvailable) return;
    setSelectedTimeSlot(timeSlot);

    // Use selected date OR fall back to next occurrence
    const targetDate = selectedDate || getNextOccurrence(selectedSlot.dayOfWeek);
    
    setVisitData((prev) => ({
      ...prev,
      startTime: `${targetDate}T${timeSlot.startTime}:00`,
      endTime: `${targetDate}T${timeSlot.endTime}:00`,
      doctorId: selectedSlot.doctor.id,
    }));
  };

  useEffect(() => {
    const fetchCollaborators = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem("token");
        const res = await axios.get(
          "http://localhost:8081/api/users/collaborators",
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        setCollaborators(res.data);
      } catch (err) {
        setCollaborators([]);
      } finally {
        setLoading(false);
      }
    };
    fetchCollaborators();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setVisitData((prev) => ({ ...prev, [name]: value }));
  };

  const validateForm = () => {
    if (!visitData.collaboratorId) {
      setMessage("❌ Veuillez sélectionner un collaborateur");
      return false;
    }
    if (!selectedTimeSlot) {
      setMessage("❌ Veuillez sélectionner un créneau horaire");
      return false;
    }
    if (!visitData.startTime || !visitData.endTime) {
      setMessage("❌ Les horaires de visite ne sont pas définis");
      return false;
    }
    if (!visitData.doctorId) {
      setMessage("❌ ID du médecin manquant");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setLoading(true);
      const token = localStorage.getItem("token");

      const requestPayload = {
        collaboratorId: parseInt(visitData.collaboratorId),
        doctorId: parseInt(visitData.doctorId),
        visitType: visitData.visitType,
        startTime: visitData.startTime,
        endTime: visitData.endTime,
      };

      await axios.post("http://localhost:8081/api/visits/with-slot", requestPayload, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      setMessage("✅ Visite planifiée avec succès !");
      setTimeout(() => onClose(), 1500);
    } catch (err) {
      console.error("Error scheduling visit:", err);
      if (err.response?.data?.message) {
        setMessage(`❌ ${err.response.data.message}`);
      } else if (err.response?.data) {
        setMessage(`❌ Erreur: ${JSON.stringify(err.response.data)}`);
      } else {
        setMessage("❌ Erreur lors de la planification.");
      }
    } finally {
      setLoading(false);
    }
  };

  if (!selectedSlot) return null;

  return (
    <div className="fixed inset-0 bg-blue-50 bg-opacity-40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-md shadow-gray-200 p-6 w-full max-w-2xl border border-gray-100 max-h-[90vh] overflow-y-auto">
        <h3 className="text-2xl font-medium text-gray-800 mb-6 text-center sticky top-0 bg-white py-2">
          Planifier une visite
        </h3>

        <form onSubmit={handleSubmit} className="space-y-5 min-h-0">
          {/* Collaborateur Selection */}
          <div>
            <label className="block font-medium mb-1 text-gray-700">Collaborateur</label>
            <select
              name="collaboratorId"
              value={visitData.collaboratorId}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
              required
              disabled={loading}
            >
              <option value="">Sélectionner un collaborateur</option>
              {collaborators.map((collab) => (
                <option key={collab.id} value={collab.id}>
                  {collab.firstName
                    ? `${collab.firstName} ${collab.lastName || ""}`
                    : collab.id}
                </option>
              ))}
            </select>
          </div>

          {/* Médecin Display (Read-only) */}
          <div>
            <label className="block font-medium mb-1 text-gray-700">Médecin</label>
            <div className="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-lg text-gray-700">
              {selectedSlot.doctor
                ? `${selectedSlot.doctor.firstName} ${selectedSlot.doctor.lastName}`
                : "Médecin non spécifié"}
            </div>
          </div>

          {/* Type de visite */}
          <div>
            <label className="block font-medium mb-1 text-gray-700">Type de visite</label>
            <select
              name="visitType"
              value={visitData.visitType}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
              disabled={loading}
            >
              <option value="HIRING">Embauche</option>
              <option value="PERIODIC">Périodique</option>
              <option value="RETURN_TO_WORK">Retour au travail</option>
              <option value="JOB_CHANGE">Changement de poste</option>
              <option value="SPONTANEOUS">Spontanée</option>
              <option value="MEDICAL_FOLLOW_UP">Suivi médical</option>
              <option value="EXCEPTIONAL_VISIT">Exceptionnelle</option>
            </select>
          </div>

          {/* ✅ Time Slot Selection - UPDATED FOR SCROLLING */}
          <div className="bg-blue-50 p-4 rounded-lg border border-blue-200 max-h-[60vh] overflow-y-auto">
            <h4 className="font-medium text-gray-800 mb-3 sticky top-0 bg-blue-50 py-2">
              Sélectionner un créneau horaire
            </h4>

            {/* ← REPLACED OLD DATE DISPLAY WITH DateScroller */}
            <DateScroller
              recurringSlot={selectedSlot}
              onDateSelect={setSelectedDate}
              selectedDate={selectedDate}
            />

            {slotLoading ? (
              <div className="flex items-center gap-2 text-blue-600 mt-4">
                <svg
                  className="w-4 h-4 animate-spin"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8v8z"
                  ></path>
                </svg>
                Chargement des créneaux disponibles...
              </div>
            ) : (
              <>
                {/* Time Slot Grid - REMOVED max-h-40 overflow-y-auto */}
                <div className="grid grid-cols-4 gap-2 mt-4">
                  {availableTimeSlots.map((slot, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => handleTimeSlotSelect(slot)}
                      disabled={!slot.isAvailable}
                      className={`p-3 text-sm rounded-lg border transition ${!slot.isAvailable
                        ? "bg-gray-200 text-gray-400 border-gray-300 cursor-not-allowed"
                        : selectedTimeSlot === slot
                          ? "bg-blue-600 text-white border-blue-600"
                          : "bg-white text-gray-700 border-gray-300 hover:bg-blue-50"
                        }`}
                    >
                      {slot.startTime} - {slot.endTime}
                    </button>
                  ))}
                </div>

                {/* Selected Slot Info */}
                {selectedTimeSlot && (
                  <div className="mt-3 p-3 bg-green-50 border border-green-200 rounded-lg">
                    <div className="text-sm text-green-800">
                      ✅ Créneau sélectionné: {selectedTimeSlot.startTime} -{" "}
                      {selectedTimeSlot.endTime}
                    </div>
                    <div className="text-xs text-green-600 mt-1">
                      Date: {selectedDate || getNextOccurrence(selectedSlot.dayOfWeek)}
                    </div>
                  </div>
                )}

                {/* Legend */}
                <div className="mt-2 text-xs text-gray-500">
                  <span className="inline-block w-3 h-3 bg-white border border-gray-300 rounded mr-1"></span>
                  Disponible
                  <span className="inline-block w-3 h-3 bg-gray-200 border border-gray-300 rounded ml-3 mr-1"></span>
                  Occupé
                </div>
              </>
            )}
          </div>

          {/* Hidden inputs */}
          <input type="hidden" name="startTime" value={visitData.startTime} />
          <input type="hidden" name="endTime" value={visitData.endTime} />
          <input type="hidden" name="doctorId" value={visitData.doctorId} />

          {/* Action Buttons - STICKY BOTTOM */}
          <div className="flex justify-end space-x-3 mt-8 sticky bottom-0 bg-white py-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition"
              disabled={loading}
            >
              Annuler
            </button>
            <button
              type="submit"
              disabled={loading || !selectedTimeSlot}
              className="px-5 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {loading ? "Planification..." : "Confirmer"}
            </button>
          </div>

          {message && (
            <p
              className={`text-center text-sm mt-4 ${message.includes("✅") ? "text-green-600" : "text-red-600"
                }`}
            >
              {message}
            </p>
          )}
        </form>
      </div>
    </div>
  );
}