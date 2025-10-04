import React, { useState, useMemo } from 'react';

const DailyHealthTip = () => {
  const tips = [
    {
      text: "Buvez au moins 1.5L d'eau par jour pour une bonne hydratation.",
      emoji: "💧",
      color: "blue"
    },
    {
      text: "Marchez 30 minutes quotidiennement pour votre santé cardiovasculaire.",
      emoji: "🚶‍♂️",
      color: "green"
    },
    {
      text: "Consommez 5 fruits et légumes par jour pour renforcer votre système immunitaire.",
      emoji: "🍎",
      color: "red"
    },
    {
      text: "Un bon sommeil (7-8h) est essentiel pour la récupération et la santé mentale.",
      emoji: "😴",
      color: "purple"
    },
    {
      text: "Lavez-vous les mains régulièrement pour éviter la propagation des infections.",
      emoji: "🧼",
      color: "indigo"
    },
    {
      text: "Pratiquez la respiration profonde pour réduire le stress et l'anxiété.",
      emoji: "🌬️",
      color: "teal"
    },
    {
      text: "Limitez votre consommation de sucre et de sel pour prévenir les maladies chroniques.",
      emoji: "🚫",
      color: "orange"
    },
    {
      text: "Faites des pauses régulières si vous travaillez assis toute la journée.",
      emoji: "⏸️",
      color: "amber"
    },
    {
      text: "Consultez régulièrement votre médecin pour des bilans de santé préventifs.",
      emoji: "👨‍⚕️",
      color: "cyan"
    },
    {
      text: "Protégez votre peau du soleil avec une crème solaire adaptée.",
      emoji: "☀️",
      color: "yellow"
    },
    {
      text: "Maintenez une posture correcte pour éviter les douleurs dorsales.",
      emoji: "🧘‍♂️",
      color: "lime"
    },
    {
      text: "Évitez de fumer et limitez votre consommation d'alcool.",
      emoji: "🚭",
      color: "pink"
    },
    {
      text: "Pratiquez une activité physique modérée au moins 3 fois par semaine.",
      emoji: "🏋️‍♂️",
      color: "emerald"
    },
    {
      text: "Mangez lentement et mastiquez bien pour une meilleure digestion.",
      emoji: "🍽️",
      color: "fuchsia"
    },
    {
      text: "Prenez le temps de vous détendre et de faire des activités que vous aimez.",
      emoji: "❤️",
      color: "rose"
    }
  ];
  
  // Utilisation de useMemo pour que le tip ne change pas à chaque rendu
  const randomTip = useMemo(() => {
    return tips[Math.floor(Math.random() * tips.length)];
  }, []); // Tableau de dépendances vide = ne change qu'au montage

  // BMI calculator state
  const [height, setHeight] = useState('');
  const [weight, setWeight] = useState('');
  const [bmi, setBmi] = useState(null);
  const [bmiMessage, setBmiMessage] = useState('');

  const handleBmiSubmit = (e) => {
    e.preventDefault();
    if (!height || !weight || isNaN(height) || isNaN(weight) || height <= 0 || weight <= 0) {
      setBmi(null);
      setBmiMessage("Veuillez entrer une taille et un poids valides.");
      return;
    }
    const heightMeters = parseFloat(height) / 100;
    const bmiValue = parseFloat(weight) / (heightMeters * heightMeters);
    setBmi(bmiValue.toFixed(1));
    
    let msg = '';
    let msgColor = '';
    if (bmiValue < 18.5) {
      msg = "Insuffisance pondérale";
      msgColor = "text-yellow-600";
    } else if (bmiValue < 25) {
      msg = "Corpulence normale";
      msgColor = "text-green-600";
    } else if (bmiValue < 30) {
      msg = "Surpoids";
      msgColor = "text-orange-600";
    } else {
      msg = "Obésité";
      msgColor = "text-red-600";
    }
    setBmiMessage(msg);
  };

  const resetBmi = () => {
    setHeight('');
    setWeight('');
    setBmi(null);
    setBmiMessage('');
  };
  
  // Mapping des couleurs aux classes Tailwind
  const colorClasses = {
    blue: "bg-blue-50 text-blue-800 border-l-blue-400",
    green: "bg-green-50 text-green-800 border-l-green-400",
    red: "bg-red-50 text-red-800 border-l-red-400",
    purple: "bg-purple-50 text-purple-800 border-l-purple-400",
    indigo: "bg-indigo-50 text-indigo-800 border-l-indigo-400",
    teal: "bg-teal-50 text-teal-800 border-l-teal-400",
    orange: "bg-orange-50 text-orange-800 border-l-orange-400",
    amber: "bg-amber-50 text-amber-800 border-l-amber-400",
    cyan: "bg-cyan-50 text-cyan-800 border-l-cyan-400",
    yellow: "bg-yellow-50 text-yellow-800 border-l-yellow-400",
    lime: "bg-lime-50 text-lime-800 border-l-lime-400",
    pink: "bg-pink-50 text-pink-800 border-l-pink-400",
    emerald: "bg-emerald-50 text-emerald-800 border-l-emerald-400",
    fuchsia: "bg-fuchsia-50 text-fuchsia-800 border-l-fuchsia-400",
    rose: "bg-rose-50 text-rose-800 border-l-rose-400"
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
      <h3 className="font-semibold text-lg mb-3 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        Conseil santé du jour
      </h3>
      
      <div className={`p-3 rounded-lg border-l-4 ${colorClasses[randomTip.color]}`}>
        <p className="text-sm">
          <span className="text-xl mr-2">{randomTip.emoji}</span>
          {randomTip.text}
        </p>
      </div>
      
      <p className="text-xs text-gray-500 mt-2 text-center">
        Actualisé quotidiennement
      </p>

      {/* BMI Calculator - Style amélioré */}
      <div className="mt-6 pt-4 border-t border-gray-100">
        <h4 className="font-semibold text-md mb-3 text-gray-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Calculateur d'IMC
        </h4>
        
        <form onSubmit={handleBmiSubmit} className="space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Taille (cm)
              </label>
              <input
                type="number"
                min="100"
                max="250"
                placeholder="Ex: 175"
                value={height}
                onChange={e => setHeight(e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Poids (kg)
              </label>
              <input
                type="number"
                min="30"
                max="200"
                placeholder="Ex: 68"
                value={weight}
                onChange={e => setWeight(e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          
          <div className="flex space-x-2">
            <button
              type="submit"
              disabled={!height || !weight}
              className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              Calculer IMC
            </button>
            
            <button
              type="button"
              onClick={resetBmi}
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Effacer
            </button>
          </div>
        </form>
        
        {bmi && (
          <div className="mt-4 p-3 bg-gray-50 rounded-md">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600 mb-1">Votre IMC: {bmi}</div>
              <div className={`font-semibold ${bmiMessage.includes('normale') ? 'text-green-600' : 
                bmiMessage.includes('pondérale') ? 'text-yellow-600' : 
                bmiMessage.includes('Surpoids') ? 'text-orange-600' : 'text-red-600'}`}>
                {bmiMessage}
              </div>
            </div>
            
            <div className="mt-3">
              <div className="flex justify-between text-xs text-gray-600 mb-1">
                <span>16</span>
                <span>18.5</span>
                <span>25</span>
                <span>30</span>
                <span>40</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{
                    width: `${Math.min(Math.max((parseFloat(bmi) - 16) / (40 - 16) * 100, 0), 100)}%`
                  }}
                ></div>
              </div>
              <div className="flex justify-between text-xs text-gray-600 mt-1">
                <span>Maigreur</span>
                <span>Normal</span>
                <span>Surpoids</span>
                <span>Obésité</span>
              </div>
            </div>
          </div>
        )}
        
        {!bmi && bmiMessage && (
          <div className="mt-3 p-2 bg-red-50 text-red-700 rounded-md text-sm text-center">
            {bmiMessage}
          </div>
        )}
      </div>
    </div>
  );
};

export default DailyHealthTip;