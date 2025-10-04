
import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { ROLES } from '../../config/roles';
import AdminDashboard from '../Admin/AdminDashboard';
import WaitingApproval from '../Auth/WaitingApproval';
import RHUserList from '../UserManagement/RHUserList';
import MedecinUserList from '../UserManagement/MedecinUserList';
import CollaborateurUserList from '../UserManagement/CollaborateurUserList';
import DoctorSlotsPage from '../Doctor/RecurringSlotsList';
import DoctorDashboard from '../Doctor/DoctorDashboard';
import AvailableSlotsViewer from '../RH/AvailableSlotsViewer';
import CollabVisits from '../Collaborator/CollabVisits';
const Dashboard = () => {
    const { user } = useAuth();

    if (!user) {
        return <div>Chargement...</div>;
    }

    switch (user.role) {
        case ROLES.ADMIN:
            return <AdminDashboard />;
        case ROLES.RH:
            return <AvailableSlotsViewer />;
        case ROLES.DOCTOR:
            return <DoctorDashboard />;
        case ROLES.COLLABORATOR:
            return <CollabVisits />;
        case ROLES.PENDING:
             return <WaitingApproval />;
        default:
            return <div>Rôle non reconnu.</div>;
    }
};

export default Dashboard;
