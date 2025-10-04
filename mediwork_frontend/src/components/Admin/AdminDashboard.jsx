import React, { useEffect, useState } from 'react';
import { userApi } from '../../api/userApi';
import DoughnutChart from './DoughnutChart';
import BarChart from './BarChart';
import PendingUsersList from '../UserManagement/PendingUsersList'; 
import StatsCard, { StatsIcons } from './StatsCard';    
const AdminDashboard = () => {
    // States pour les stats utilisateurs
    const [totalUsers, setTotalUsers] = useState(0);
    const [totalRH, setTotalRH] = useState(0);
    const [totalMedecin, setTotalMedecin] = useState(0);
    const [totalCollaborateur, setTotalCollaborateur] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


    useEffect(() => {
        const fetchStats = async () => {
            setLoading(true);
            setError(null);
            try {
                const users = await userApi.getUsers();
                setTotalUsers(Array.isArray(users) ? users.length : 0);

                const rh = await userApi.getRHUsers();
                setTotalRH(Array.isArray(rh) ? rh.length : 0);

                const medecins = await userApi.getMedecinUsers();
                setTotalMedecin(Array.isArray(medecins) ? medecins.length : 0);

                const collaborateurs = await userApi.getCollaborateurUsers();
                setTotalCollaborateur(Array.isArray(collaborateurs) ? collaborateurs.length : 0);
            } catch (err) {
                setError(err.message || 'Erreur lors du chargement des statistiques');
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);


    const statsData = [
        { title: "Total Users", value: totalUsers, icon: StatsIcons.totalUsers, color: "purple" },
        { title: "Total RH", value: totalRH, icon: StatsIcons.rhUsers, color: "blue" },
        { title: "Total Médecins", value: totalMedecin, icon: StatsIcons.medecinUsers, color: "green" },
        { title: "Total Collaborateurs", value: totalCollaborateur, icon: StatsIcons.collaborateurUsers, color: "orange" }
    ];

    const monthlyData = [];

    // Répartition par rôle pour le DoughnutChart (données dynamiques)
    const userDistributionData = {
        labels: ['Médecins', 'RH', 'Collaborateurs'],
        datasets: [{
            label: 'Répartition des rôles',
            data: [totalMedecin, totalRH, totalCollaborateur],
            backgroundColor: [
                'rgba(54, 162, 235, 0.8)', // Médecins
                'rgba(153, 102, 255, 0.8)', // RH
                'rgba(255, 159, 64, 0.8)', // Collaborateurs
            ],
            borderColor: [
                'rgba(54, 162, 235, 1)',
                'rgba(153, 102, 255, 1)',
                'rgba(255, 159, 64, 1)',
            ],
            borderWidth: 2
        }]
    };

    return (
        <div className="flex-1 pb-12 space-y-6 md:space-y-8">
            {/* Header Section */}
            <section className="flex flex-col w-full px-6 md:justify-between md:items-center md:flex-row">
                <div>
                    <h2 className="text-3xl font-medium text-gray-800">Dashboard</h2>
                    <p className="mt-2 text-sm text-gray-500">Medical Visit Management System</p>
                </div>
                
            </section>

            {/* ✅ Utilisateurs en attente d'approbation */}
            <section className="px-6">
                <PendingUsersList />
            </section>

            {/* Stats Cards & Charts */}
            <section className="grid grid-cols-1 gap-8 px-6 xl:grid-cols-3 2xl:grid-cols-4 md:grid-cols-2">
                {loading ? (
                    <div className="col-span-full text-center text-gray-500">Chargement des statistiques...</div>
                ) : error ? (
                    <div className="col-span-full text-center text-red-500">{error}</div>
                ) : (
                    statsData.map((stat, index) => (
                        <StatsCard
                            key={index}
                            title={stat.title}
                            value={stat.value}
                            icon={stat.icon}
                            color={stat.color}
                        />
                    ))
                )}
                {/* <BarChart 
                    data={monthlyData}
                    title="Monthly User Activity"
                    legends={true}
                /> */}
                <DoughnutChart 
                    data={userDistributionData}
                    title="User Status Distribution"
                />
            </section>

            {/* Recent Activity */}
            <section className="px-6">
                <div className="bg-white rounded-lg shadow-md shadow-gray-200">
                    <div className="px-6 py-4 border-b border-gray-200">
                        <h3 className="text-lg font-medium text-gray-800">Recent Activity</h3>
                    </div>
                    <div className="p-6">
                        <div className="space-y-4">
                            {[
                                { action: "New appointment scheduled", user: "Dr. Smith", time: "2 minutes ago", type: "appointment" },
                                { action: "User registration completed", user: "John Doe", time: "15 minutes ago", type: "user" },
                                { action: "Medical report uploaded", user: "Dr. Johnson", time: "1 hour ago", type: "report" },
                                { action: "System backup completed", user: "System", time: "2 hours ago", type: "system" }
                            ].map((activity, index) => (
                                <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div className="flex items-center space-x-3">
                                        <div className={`w-2 h-2 rounded-full ${
                                            activity.type === 'appointment' ? 'bg-blue-500' :
                                            activity.type === 'user' ? 'bg-green-500' :
                                            activity.type === 'report' ? 'bg-purple-500' : 'bg-gray-500'
                                        }`} />
                                        <div>
                                            <p className="text-sm font-medium text-gray-800">{activity.action}</p>
                                            <p className="text-xs text-gray-500">by {activity.user}</p>
                                        </div>
                                    </div>
                                    <span className="text-xs text-gray-400">{activity.time}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
}; 

export default AdminDashboard;
