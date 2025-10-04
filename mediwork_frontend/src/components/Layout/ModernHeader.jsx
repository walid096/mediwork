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
        <header className="flex items-center justify-between h-20 px-6 bg-white border-b">
            <div className="relative flex items-center">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                    <svg className="w-5 h-5 text-gray-400" viewBox="0 0 24 24" fill="none">
                        <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"></path>
                    </svg>
                </span>
                
                <input 
                    type="text" 
                    className="py-2.5 pl-10 pr-4 text-gray-700 placeholder-gray-400 bg-white border border-gray-200 rounded-lg sm:w-auto w-36 focus:border-blue-400 focus:outline-none focus:ring focus:ring-blue-300 focus:ring-opacity-40" 
                    placeholder="Search" 
                />
            </div>

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

                <button className="relative p-2 mx-3 text-gray-400 transition-colors duration-300 rounded-full hover:bg-gray-100 hover:text-gray-600 focus:bg-gray-100">
                    <span className="sr-only">Notifications</span>
                    <span className="absolute top-0 right-0 w-2 h-2 mt-1 mr-2 bg-blue-700 rounded-full"></span>
                    <span className="absolute top-0 right-0 w-2 h-2 mt-1 mr-2 bg-blue-700 rounded-full animate-ping"></span>
                    
                    <svg aria-hidden="true" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-6 h-6">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                </button>

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
