import React from 'react';
import { useAuth } from '../../contexts/AuthContext';

const DoctorInfoCard = () => {
    const { user } = useAuth();

    // fallback statique si user absent ou incomplet
    const fullname = user?.fullName || 'Ahmed';
    const departement = user?.department || 'Généraliste';

    return (
        <div className="bg-white rounded-xl shadow-md p-4 mb-4 border border-gray-100 flex items-center w-[29rem]">
            {/* Icône du docteur */}
            <div className="bg-blue-100 p-3 rounded-full mr-3 flex-shrink-0">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
            </div>
            
            {/* Informations du docteur */}
            <div className="text-right">
                <h2 className="text-md font-semibold text-gray-800 m-0 leading-tight">
                    Dr. {fullname} 
                </h2>
                <p className="text-sm text-gray-600 mt-1 mb-0">
                    <span className="text-green-600 font-medium">{departement}</span>
                </p>
            </div>
        </div>
    );
};

export default DoctorInfoCard;