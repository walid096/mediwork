import React, { useState } from 'react';
import ModernSidebar from './Sidebar';
import HeaderComponent from './HeaderComponent';
import MobileHeader from './MobileHeader';

const Layout = ({ children }) => {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    return (
        <div className="relative min-h-screen lg:flex">
            {/* Mobile Header */}
            <MobileHeader 
                isOpen={sidebarOpen} 
                onToggle={() => setSidebarOpen(!sidebarOpen)} 
            />

            {/* Sidebar Overlay for mobile */}
            <div 
                className={`${sidebarOpen ? 'block' : 'hidden'} fixed inset-0 z-20 transition-opacity bg-black opacity-30 lg:hidden`} 
                onClick={() => setSidebarOpen(false)}
            ></div>

            {/* Sidebar */}
            <ModernSidebar 
                isOpen={sidebarOpen} 
                onClose={() => setSidebarOpen(false)} 
            />

            {/* Main Content */}
            <main id="content" className="flex-1 flex flex-col ">
                {/* Desktop Header */}
                <div className="hidden lg:block ">
                    <HeaderComponent onMenuClick={() => setSidebarOpen(!sidebarOpen)} />
                </div>

                {/* Page Content */}
                <div className="flex-1 bg-gray-100 pt-6">
                    {children}
                </div>
            </main>
        </div>
    );
};

export default Layout;
