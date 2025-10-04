import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { ROLES } from '../../config/roles';
import { ClipboardPlus, Hospital, User, UserSearch, CalendarClock, Calendar } from 'lucide-react';

const Sidebar = ({ isOpen, onClose }) => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const menuItems = [
        {
            text: 'Dashboard',
            path: '/dashboard',
            roles: [ROLES.ADMIN, ROLES.RH, ROLES.DOCTOR, ROLES.COLLABORATOR],
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
            )
        },
        {
            text: 'RH Users',
            path: '/rh-users',
            roles: [ROLES.ADMIN],
            icon: (
             <UserSearch/>
            )
        },
        {
            text: 'Medecin Users',
            path: '/medecin-users',
            roles: [ROLES.ADMIN],
            icon: (
               <ClipboardPlus/>
            )
        },
        {
            text: 'Collaborateur Users',
            path: '/collaborateur-users',
            roles: [ROLES.ADMIN],
            icon: (
            <User/>
            )
        },
        {
            text: 'Mes créneaux',
            path: '/doctor-slots',
            roles: [ROLES.DOCTOR],
            icon: (
                <CalendarClock />
            )
        },
        {
            text: 'Mes visites',
            path: '/doctor-visits',
            roles: [ROLES.DOCTOR],
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            )
        },
        {
            text: 'Demandes spontanées',
            path: '/rh-spontaneous-visits',
            roles: [ROLES.RH],
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
            )
        },
        {
            text: 'Mes visites assignées',
            path: '/collab-assigned-visits',
            roles: [ROLES.COLLABORATOR],
            icon: (
                <Calendar />
            )
        }
    ];

    const handleNavigation = (path) => {
        navigate(path);
        if (onClose) onClose();
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isActive = (path) => location.pathname === path;

    return (
        <aside 
            id="sidebar" 
            className={`${isOpen ? 'translate-x-0 ease-in' : '-translate-x-full ease-out'} fixed inset-y-0 left-0 z-30 flex flex-col w-[4.5rem] min-h-screen space-y-6 overflow-y-auto text-gray-100 transition duration-200 transform bg-gray-800 lg:translate-x-0 lg:relative lg:inset-0`}
        >
            <div className="flex flex-col items-center flex-1 space-y-6">
                {/* Logo */}
                <a href="#" className="flex items-center justify-center w-full p-5 lg:p-0 lg:h-20 font-bold text-white truncate bg-blue-600 whitespace-nowrap">
                  <Hospital />
                </a>

                {/* Navigation Items */}
                <nav className="flex flex-col items-center space-y-6">
                    {menuItems
                        .filter(item => item.roles.includes(user?.role))
                        .map((item) => (
                            <button
                                key={item.text}
                                onClick={() => handleNavigation(item.path)}
                                className={`p-3 transition-colors duration-300 rounded-lg hover:bg-white ${
                                    isActive(item.path) 
                                        ? 'bg-white text-blue-700' 
                                        : 'text-gray-300 hover:text-blue-700'
                                }`}
                                title={item.text}
                            >
                                {item.icon}
                            </button>
                        ))}
                </nav>
            </div>
        </aside>
    );
};

export default Sidebar;