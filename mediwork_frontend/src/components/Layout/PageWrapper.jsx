import React from 'react';

const PageWrapper = ({ title, subtitle, children, actions }) => {
    return (
        <div className="flex-1 pb-12 space-y-6 overflow-y-auto bg-gray-100 lg:h-screen md:space-y-8">
            {/* Page Header */}
            <section className="flex flex-col w-full px-6 md:justify-between md:items-center md:flex-row">
                <div>
                    <h2 className="text-3xl font-medium text-gray-800">{title}</h2>
                    {subtitle && <p className="mt-2 text-sm text-gray-500">{subtitle}</p>}
                </div>

                {actions && (
                    <div className="flex flex-col mt-6 md:flex-row md:-mx-1 md:mt-0">
                        {actions}
                    </div>
                )}
            </section>

            {/* Page Content */}
            <section className="px-6">
                <div className="bg-white rounded-lg shadow-md shadow-gray-200 overflow-hidden">
                    {children}
                </div>
            </section>
        </div>
    );
};

export default PageWrapper;
