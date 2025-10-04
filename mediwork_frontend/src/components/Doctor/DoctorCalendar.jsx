import React, { useEffect, useState } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import { visitApi } from "../../api/visitApi";
import { useNavigate } from "react-router-dom";

function getMonthDaysWithVisits(visits) {
  // On prend la date de slot.startTime pour les visites planifiées
  const days = new Set();
  visits.forEach((v) => {
    let dateStr = null;
    if (v.slot && v.slot.startTime) {
      dateStr = v.slot.startTime;
    } else if (v.date) {
      dateStr = v.date;
    } else if (v.startDate) {
      dateStr = v.startDate;
    } else if (v.visitDate) {
      dateStr = v.visitDate;
    }
    if (dateStr) {
      // Prendre uniquement la partie date (YYYY-MM-DD) sans conversion fuseau horaire
      const onlyDate = dateStr.slice(0, 10);
      days.add(onlyDate);
    }
  });
  return days;
}

const DoctorCalendar = () => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [value, setValue] = useState(new Date());
  const navigate = useNavigate();

  useEffect(() => {
    visitApi
      .getMyVisits()
      .then((data) => {
        setVisits(data);
        setLoading(false);
      })
      .catch((e) => {
        setError(e.message);
        setLoading(false);
      });
  }, []);

  const daysWithVisits = getMonthDaysWithVisits(visits);

  const tileClassName = ({ date, view }) => {
    if (view === "month") {
      // Utiliser la date locale (YYYY-MM-DD) pour correspondre à la logique ci-dessus
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const localDate = `${year}-${month}-${day}`;
      if (daysWithVisits.has(localDate)) {
        return "visit-day";
      }
    }
    return null;
  };

  const onClickDay = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const localDate = `${year}-${month}-${day}`;
    if (daysWithVisits.has(localDate)) {
      navigate("/doctor-visits");
    }
  };

  if (loading) return (
    <div className="flex items-center justify-start h-64 ml-4">
      <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
      <span className="ml-3 text-gray-600">Chargement du calendrier...</span>
    </div>
  );
  
  if (error) return (
    <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 ml-4 max-w-md" role="alert">
      <p className="font-bold">Erreur</p>
      <p>{error}</p>
    </div>
  );

  return (
    <div className="ml-4 max-w-md bg-white rounded-xl shadow-lg overflow-hidden">
      <div className="p-5">
        <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-2 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          Calendrier des Visites
        </h2>
        <div className="react-calendar-custom">
          <Calendar
            onChange={setValue}
            value={value}
            tileClassName={tileClassName}
            onClickDay={onClickDay}
            className="border-0 w-full"
          />
        </div>
        <div className="mt-4 flex items-center">
          <div className="w-4 h-4 bg-red-500 rounded-full mr-2"></div>
          <span className="text-sm text-gray-600">Journées avec des visites programmées</span>
        </div>
      </div>

      <style>{`
        .react-calendar-custom {
          width: 100%;
        }
        
        .react-calendar-custom .react-calendar {
          width: 100%;
          border: none;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
          box-shadow: none;
        }
        
        .react-calendar-custom .react-calendar__navigation {
          display: flex;
          height: 44px;
          margin-bottom: 1em;
          background: transparent;
        }
        
        .react-calendar-custom .react-calendar__navigation button {
          min-width: 44px;
          background: none;
          color: #374151;
          font-weight: 600;
          font-size: 14px;
        }
        
        .react-calendar-custom .react-calendar__navigation button:enabled:hover,
        .react-calendar-custom .react-calendar__navigation button:enabled:focus {
          background-color: #f3f4f6;
          border-radius: 8px;
        }
        
        .react-calendar-custom .react-calendar__navigation button[disabled] {
          background-color: #f9fafb;
          color: #9ca3af;
          border-radius: 8px;
        }
        
        .react-calendar-custom .react-calendar__month-view__weekdays {
          text-align: center;
          text-transform: uppercase;
          font-weight: 600;
          font-size: 0.75em;
          color: #6b7280;
          margin-bottom: 8px;
          border-bottom: 1px solid #f3f4f6;
          padding-bottom: 8px;
        }
        
        .react-calendar-custom .react-calendar__month-view__weekdays__weekday {
          padding: 0.5em;
        }
        
        .react-calendar-custom .react-calendar__month-view__weekdays__weekday abbr {
          text-decoration: none;
        }
        
        .react-calendar-custom .react-calendar__tile {
          max-width: 100%;
          padding: 10px 6.6667px;
          background: none;
          text-align: center;
          line-height: 16px;
          font-weight: 500;
          color: #374151;
          border-radius: 8px;
          height: 36px;
        }
        
        .react-calendar-custom .react-calendar__tile:enabled:hover,
        .react-calendar-custom .react-calendar__tile:enabled:focus {
          background-color: #e0f2fe;
          color: #0369a1;
        }
        
        .react-calendar-custom .react-calendar__tile--now {
          background: #dbeafe;
          color: #1e40af;
          font-weight: 700;
        }
        
        .react-calendar-custom .react-calendar__tile--now:enabled:hover,
        .react-calendar-custom .react-calendar__tile--now:enabled:focus {
          background: #bfdbfe;
          color: #1e3a8a;
        }
        
        .react-calendar-custom .react-calendar__tile--hasActive {
          background: #76baff;
        }
        
        .react-calendar-custom .react-calendar__tile--active {
          background: #3b82f6;
          color: white;
          font-weight: 700;
        }
        
        .react-calendar-custom .react-calendar__tile--active:enabled:hover,
        .react-calendar-custom .react-calendar__tile--active:enabled:focus {
          background: #2563eb;
        }
        
        .visit-day {
          background: #ef4444 !important;
          color: white !important;
          font-weight: 700;
          border-radius: 8px;
        }
        
        .react-calendar-custom .react-calendar__tile:disabled {
          background-color: #f9fafb;
          color: #d1d5db;
        }
        
        .react-calendar-custom .react-calendar__year-view .react-calendar__tile,
        .react-calendar-custom .react-calendar__decade-view .react-calendar__tile,
        .react-calendar-custom .react-calendar__century-view .react-calendar__tile {
          padding: 2em 0.5em;
        }
      `}</style>
    </div>
  );
};

export default DoctorCalendar;