import React from 'react';

const StatsCard = ({ title, value, icon, color = "blue" }) => {
    const colorMap = {
        blue: {
            bg: "bg-blue-50",
            iconBg: "fill-blue-100",
            iconStroke: "stroke-blue-600",
            fillOpacity: "fill-opacity-5"
        },
        purple: {
            bg: "bg-purple-50", 
            iconBg: "fill-purple-100",
            iconStroke: "stroke-purple-600",
            fillOpacity: "fill-opacity-5"
        },
        green: {
            bg: "bg-green-50",
            iconBg: "fill-green-100", 
            iconStroke: "stroke-green-600",
            fillOpacity: "fill-opacity-5"
        },
        orange: {
            bg: "bg-orange-50",
            iconBg: "fill-orange-100",
            iconStroke: "stroke-orange-600", 
            fillOpacity: "fill-opacity-5"
        }
    };

    const colors = colorMap[color] || colorMap.blue;

    return (
        <div className="flex items-center px-6 py-8 bg-white rounded-lg shadow-md shadow-gray-200">
            <div className="flex items-center -mx-2">
                <div className="mx-2">
                    <svg 
                        width="70" 
                        height="70" 
                        viewBox="0 0 70 70" 
                        className={`${colors.bg}`}
                        fill="none" 
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <circle cx="35" cy="35" r="35" className={`${colors.iconBg} ${colors.fillOpacity}`} />
                        {icon || (
                            <path 
                                d="M26 44C26 40.625 30.5 40.625 32.75 38.375C33.875 37.25 30.5 37.25 30.5 31.625C30.5 27.8754 31.9996 26 35 26C38.0004 26 39.5 27.8754 39.5 31.625C39.5 37.25 36.125 37.25 37.25 38.375C39.5 40.625 44 40.625 44 44" 
                                className={colors.iconStroke}
                                strokeWidth="2" 
                                strokeLinecap="square" 
                            />
                        )}
                    </svg>
                </div>

                <div className="mx-2">
                    <h3 className="text-2xl font-medium text-gray-800">{value}</h3>
                    <p className="mt-1 text-sm text-gray-500">{title}</p>
                </div>
            </div>
        </div>
    );
};

// Pre-defined icons for common stats
export const StatsIcons = {
    users: (
        <path 
            d="M26 44C26 40.625 30.5 40.625 32.75 38.375C33.875 37.25 30.5 37.25 30.5 31.625C30.5 27.8754 31.9996 26 35 26C38.0004 26 39.5 27.8754 39.5 31.625C39.5 37.25 36.125 37.25 37.25 38.375C39.5 40.625 44 40.625 44 44" 
            strokeWidth="2" 
            strokeLinecap="square" 
        />
    ),
    totalUsers: (
        <g>
            <path d="M30 35C33.866 35 37 31.866 37 28C37 24.134 33.866 21 30 21C26.134 21 23 24.134 23 28C23 31.866 26.134 35 30 35Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M40 35C42.7614 35 45 32.7614 45 30C45 27.2386 42.7614 25 40 25C37.2386 25 35 27.2386 35 30C35 32.7614 37.2386 35 40 35Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M23 49V47C23 44.7909 23.8429 42.6586 25.3431 41.1584C26.8434 39.6581 28.9757 38.8152 31.1848 38.8152H28.8152C26.6061 38.8152 24.4738 39.6581 22.9736 41.1584C21.4733 42.6586 20.6304 44.7909 20.6304 47V49H23Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M47 49V47C47 44.7909 46.1571 42.6586 44.6569 41.1584C43.1566 39.6581 41.0243 38.8152 38.8152 38.8152H41.1848C43.3939 38.8152 45.5262 39.6581 47.0264 41.1584C48.5267 42.6586 49.3696 44.7909 49.3696 47V49H47Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
    ),
    activeUsers: (
        <g>
            <circle cx="35" cy="30" r="8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M47 49V47C47 43.6863 45.4713 40.548 42.7843 38.5 C40.0974 36.452 36.5913 35.75 33 35.75 C29.4087 35.75 25.9026 36.452 23.2157 38.5 C20.5287 40.548 19 43.6863 19 47V49H47Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <circle cx="42" cy="25" r="3" fill="currentColor"/>
        </g>
    ),
    archivedUsers: (
        <g>
            <circle cx="35" cy="30" r="8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M47 49V47C47 43.6863 45.4713 40.548 42.7843 38.5 C40.0974 36.452 36.5913 35.75 33 35.75 C29.4087 35.75 25.9026 36.452 23.2157 38.5 C20.5287 40.548 19 43.6863 19 47V49H47Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M30 30L40 30M35 25L35 35" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
    ),
    rhUsers: (
        <g>
            <rect x="22" y="24" width="26" height="22" rx="2" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M28 24V22C28 20.8954 28.8954 20 30 20H40C41.1046 20 42 20.8954 42 22V24" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M35 30C36.6569 30 38 31.3431 38 33C38 34.6569 36.6569 36 35 36C33.3431 36 32 34.6569 32 33C32 31.3431 33.3431 30 35 30Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M30 42V40C30 39.4696 30.2107 38.9609 30.5858 38.5858C30.9609 38.2107 31.4696 38 32 38H38C38.5304 38 39.0391 38.2107 39.4142 38.5858C39.7893 38.9609 40 39.4696 40 40V42" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
    ),
    medecinUsers: (
        <g>
            <path d="M35 25V45M25 35H45" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <circle cx="35" cy="35" r="15" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
    ),
    collaborateurUsers: (
        <g>
            <path d="M25 30H45M25 36H45M25 42H45M30 22V18M40 22V18M23 22H47C48.1046 22 49 22.8954 49 24V46C49 47.1046 48.1046 48 47 48H23C21.8954 48 21 47.1046 21 46V24C21 22.8954 21.8954 22 23 22Z" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
    ),
    appointments: (
        <path 
            d="M25 32H45M25 38H45M25 44H45M30 26V20M40 26V20M23 26H47C48.1046 26 49 26.8954 49 28V46C49 47.1046 48.1046 48 47 48H23C21.8954 48 21 47.1046 21 46V28C21 26.8954 21.8954 26 23 26Z" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round" 
        />
    ),
    revenue: (
        <path 
            d="M35 23V47M43 31L35 23L27 31M35 47L45 45M35 47L25 45" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round" 
        />
    ),
    tasks: (
        <path 
            d="M25 32L30 37L45 22M23 26H47C48.1046 26 49 26.8954 49 28V46C49 47.1046 48.1046 48 47 48H23C21.8954 48 21 47.1046 21 46V28C21 26.8954 21.8954 26 23 26Z" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round" 
        />
    )
};

export default StatsCard;
