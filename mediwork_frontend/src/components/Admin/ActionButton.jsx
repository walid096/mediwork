import React from 'react';
import { useNavigate } from 'react-router-dom';

const ActionButton = ({ title, path, icon, variant = "primary" }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(path);
    };

    const baseClasses = "px-6 py-3 focus:outline-none transition-colors duration-300 rounded-lg flex items-center justify-center";
    const variantClasses = {
        primary: "text-white bg-blue-600 hover:bg-blue-500",
        secondary: "text-gray-500 hover:bg-gray-400 hover:text-white",
        success: "text-white bg-green-600 hover:bg-green-500",
        warning: "text-white bg-orange-600 hover:bg-orange-500",
        danger: "text-white bg-red-600 hover:bg-red-500",
    };

    return (
        <button 
            onClick={handleClick}
            className={`${baseClasses} ${variantClasses[variant]}`}
        >
            <div className="flex items-center justify-center -mx-1">
                {icon && (
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5 mx-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        {icon}
                    </svg>
                )}
                <span className="mx-1 text-sm">{title}</span>
            </div>
        </button>
    );
};

// Pre-defined icons for common actions
export const ActionIcons = {
    people: <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.5m-4.5 0h1.5m-1.5 0v1.5m0-1.5v-1.5" />,
    hospital: <path strokeLinecap="round" strokeLinejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />,
    user: <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />,
    plus: <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />,
    edit: <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
};

export default ActionButton;
