import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';

const HeaderComponent = ({ onMenuClick }) => {
    const { user, logout } = useAuth();
    const [dropdownOpen, setDropdownOpen] = useState(false);

    const handleLogout = () => {
        logout();
        setDropdownOpen(false);
    };

    return (
        <header className="flex justify-end h-20 px-6 bg-white">          
            
            <div className="flex items-center">
                <div className="relative">
                    <button 
                        className="transition-colors duration-300 rounded-lg sm:px-4 sm:py-2 focus:outline-none hover:bg-gray-100" 
                        onClick={() => setDropdownOpen(!dropdownOpen)}
                    >
                        <span className="sr-only">User Menu</span>
                        <div className="flex items-center md:-mx-2">
                            <div className="hidden md:mx-2 md:flex md:flex-col md:items-end md:leading-tight">
                                <span className="font-semibold text-sm text-gray-800">{user?.fullName || user?.firstName}</span>
                                <span className="text-sm text-gray-600">{user?.role}</span>
                            </div>
    
                            <div className="flex-shrink-0 w-10 h-10 overflow-hidden bg-blue-600 rounded-full md:mx-2 flex items-center justify-center">
                                <span className="text-white font-medium">
                                    {user?.firstName?.charAt(0) || user?.fullName?.charAt(0) || 'U'}
                                </span>
                            </div>
                        </div>
                    </button>
                    
                    {dropdownOpen && (
                        <div className="absolute right-0 z-50 w-56 p-2 bg-white border rounded-lg shadow-lg top-16 lg:top-20">
                            <div className="px-4 py-2 text-gray-800 transition-colors duration-300 rounded-lg cursor-pointer hover:bg-gray-100">
                                Profile
                            </div>
                            <div className="px-4 py-2 text-gray-800 transition-colors duration-300 rounded-lg cursor-pointer hover:bg-gray-100">
                                Settings
                            </div>
                            <div className="border-t my-1"></div>
                            <div 
                                className="px-4 py-2 text-gray-800 transition-colors duration-300 rounded-lg cursor-pointer hover:bg-gray-100"
                                onClick={handleLogout}
                            >
                                Logout
                            </div>
                        </div>
                    )}
                </div>

                {dropdownOpen && <div className="fixed inset-0 z-30" onClick={() => setDropdownOpen(false)}></div>}

                
                <button 
                    className="p-2 text-gray-400 transition-colors duration-300 rounded-full focus:outline-none hover:bg-gray-100 hover:text-gray-600 focus:bg-gray-100"
                    onClick={handleLogout}
                >
                    <span className="sr-only">Log out</span>
                    
                    <svg aria-hidden="true" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-6 h-6">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                </button>
            </div>
        </header>
    );
};

export default HeaderComponent;
