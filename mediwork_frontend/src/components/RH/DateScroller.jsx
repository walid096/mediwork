import React, { useState, useEffect } from "react";

const DateScroller = ({ recurringSlot, onDateSelect, selectedDate }) => {
  const [availableDates, setAvailableDates] = useState([]);
  const [loading, setLoading] = useState(false);

  // Generate next 4 weeks of dates for the recurring slot pattern
  const generateDateRange = (recurringSlot) => {
    if (!recurringSlot || !recurringSlot.dayOfWeek) return [];

    const dates = [];
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

    const targetDay = dayMap[recurringSlot.dayOfWeek];
    if (targetDay === undefined) return [];

    // Generate dates for next 4 weeks
    for (let week = 0; week < 4; week++) {
      for (let day = 0; day < 7; day++) {
        const date = new Date(today);
        const currentDay = today.getDay();
        
        // Calculate days until next occurrence of target day
        let daysUntilTarget = targetDay - currentDay;
        if (daysUntilTarget <= 0) {
          daysUntilTarget += 7; // Next week
        }
        
        // Add weeks to get future dates
        const targetDate = new Date(today);
        targetDate.setDate(today.getDate() + daysUntilTarget + (week * 7));
        
        // Only add dates that are in the future
        if (targetDate > today) {
          dates.push({
            date: targetDate.toISOString().split("T")[0], // YYYY-MM-DD format
            dayOfWeek: recurringSlot.dayOfWeek,
            displayDate: targetDate.getDate(),
            displayMonth: targetDate.toLocaleDateString('fr-FR', { month: 'short' }),
            displayDay: targetDate.toLocaleDateString('fr-FR', { weekday: 'short' }),
            isAvailable: true, // Will be updated with conflict checking
            isToday: targetDate.toDateString() === today.toDateString(),
            isSelected: false, // Will be updated based on selectedDate prop
          });
        }
      }
    }

    // Remove duplicates and sort by date
    const uniqueDates = dates.filter((date, index, self) => 
      index === self.findIndex(d => d.date === date.date)
    ).sort((a, b) => new Date(a.date) - new Date(b.date));

    return uniqueDates;
  };

  // Update available dates when recurring slot changes
  useEffect(() => {
    if (recurringSlot) {
      setLoading(true);
      const dates = generateDateRange(recurringSlot);
      
      // Mark selected date
      const datesWithSelection = dates.map(date => ({
        ...date,
        isSelected: selectedDate === date.date
      }));
      
      setAvailableDates(datesWithSelection);
      setLoading(false);
    }
  }, [recurringSlot, selectedDate]);

  // Handle date selection
  const handleDateSelect = (date) => {
    if (onDateSelect) {
      onDateSelect(date);
    }
  };

  // Format date for display
  const formatDateDisplay = (dateObj) => {
    if (dateObj.isToday) {
      return "Aujourd'hui";
    }
    return `${dateObj.displayDay} ${dateObj.displayDate}`;
  };

  // Get availability status text
  const getAvailabilityText = (dateObj) => {
    if (dateObj.isToday) {
      return "Aujourd'hui";
    }
    return `${dateObj.displayMonth}`;
  };

  if (!recurringSlot) {
    return (
      <div className="text-center text-gray-500 py-4">
        Aucun créneau récurrent sélectionné
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center py-4">
        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
        <span className="ml-2 text-gray-600">Génération des dates...</span>
      </div>
    );
  }

  if (availableDates.length === 0) {
    return (
      <div className="text-center text-gray-500 py-4">
        Aucune date disponible pour ce créneau récurrent
      </div>
    );
  }

  return (
    <div className="w-full">
      {/* Header */}
      <div className="mb-3">
        <h5 className="font-medium text-gray-800 mb-1">
          Sélectionner une date
        </h5>
        <p className="text-sm text-gray-600">
          Disponibilité: {recurringSlot.dayOfWeek} de {recurringSlot.startTime} à {recurringSlot.endTime}
        </p>
      </div>

      {/* Horizontal Date Scroller */}
      <div className="relative">
        {/* Left Scroll Button */}
        <button
          className="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-white border border-gray-200 rounded-full p-2 shadow-md hover:bg-gray-50 transition-colors"
          onClick={() => {
            const container = document.getElementById('date-scroll-container');
            if (container) {
              container.scrollLeft -= 200;
            }
          }}
        >
          <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        {/* Right Scroll Button */}
        <button
          className="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-white border border-gray-200 rounded-full p-2 shadow-md hover:bg-gray-50 transition-colors"
          onClick={() => {
            const container = document.getElementById('date-scroll-container');
            if (container) {
              container.scrollLeft += 200;
            }
          }}
        >
          <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>

        {/* Date Container with Horizontal Scroll */}
        <div
          id="date-scroll-container"
          className="flex space-x-3 overflow-x-auto scrollbar-hide px-8 py-2"
          style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
        >
          {availableDates.map((dateObj, index) => (
            <button
              key={index}
              onClick={() => handleDateSelect(dateObj.date)}
              className={`flex-shrink-0 w-20 h-24 flex flex-col items-center justify-center rounded-lg border-2 transition-all duration-200 hover:shadow-md ${
                dateObj.isSelected
                  ? 'border-blue-600 bg-blue-50 text-blue-700 shadow-md'
                  : dateObj.isToday
                  ? 'border-green-500 bg-green-50 text-green-700'
                  : 'border-gray-200 bg-white text-gray-700 hover:border-gray-300 hover:bg-gray-50'
              }`}
            >
              {/* Day of Week */}
              <div className={`text-xs font-medium mb-1 ${
                dateObj.isSelected ? 'text-blue-600' : 'text-gray-500'
              }`}>
                {dateObj.displayDay}
              </div>
              
              {/* Date Number */}
              <div className={`text-xl font-bold mb-1 ${
                dateObj.isSelected ? 'text-blue-700' : 'text-gray-800'
              }`}>
                {dateObj.displayDate}
              </div>
              
              {/* Month */}
              <div className={`text-xs ${
                dateObj.isSelected ? 'text-blue-600' : 'text-gray-500'
              }`}>
                {getAvailabilityText(dateObj)}
              </div>

              {/* Selection Indicator */}
              {dateObj.isSelected && (
                <div className="absolute -top-1 -right-1 w-4 h-4 bg-blue-600 rounded-full flex items-center justify-center">
                  <svg className="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                </div>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Selected Date Info */}
      {selectedDate && (
        <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="text-sm text-blue-800">
            ✅ Date sélectionnée: {selectedDate}
          </div>
          <div className="text-xs text-blue-600 mt-1">
            Créneaux disponibles pour cette date
          </div>
        </div>
      )}

      {/* Legend */}
      <div className="mt-3 flex items-center justify-center space-x-4 text-xs text-gray-500">
        <div className="flex items-center">
          <div className="w-3 h-3 bg-white border-2 border-gray-200 rounded mr-1"></div>
          Disponible
        </div>
        <div className="flex items-center">
          <div className="w-3 h-3 bg-green-50 border-2 border-green-500 rounded mr-1"></div>
          Aujourd'hui
        </div>
        <div className="flex items-center">
          <div className="w-3 h-3 bg-blue-50 border-2 border-blue-600 rounded mr-1"></div>
          Sélectionné
        </div>
      </div>
    </div>
  );
};

export default DateScroller;