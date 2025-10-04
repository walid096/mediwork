import React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import LoginForm from './components/Auth/LoginForm';
import Dashboard from './components/Dashboard/Dashboard';
import MainLayout from './components/Layout/MainLayout';
import ProtectedRoute from './components/Layout/ProtectedRoute';
import CollaborateurUserList from './components/UserManagement/CollaborateurUserList';
import MedecinUserList from './components/UserManagement/MedecinUserList';
import RHUserList from './components/UserManagement/RHUserList';
import { ROLES } from './config/roles';
import { AuthProvider } from './contexts/AuthContext';
import RegisterForm from './components/Auth/RegisterForm';
import WaitingApproval from './components/Auth/WaitingApproval';
import PendingUsersList from './components/UserManagement/PendingUsersList';
import DoctorSlotsPage from './components/Doctor/RecurringSlotsList';
import RecurringSlotsManager from './components/Doctor/RecurringSlotManager';
import VisitList from './components/Doctor/VisitList';
import CollabVisits from './components/Collaborator/CollabVisits';
import CollabAssignedVisits from './components/Collaborator/CollabAssignedVisits';
import SpontaneousVisitsDemands from './components/RH/SpontaneousVisitsDemands';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#121212',
      paper: '#1e1e1e',
    },
  },
});
function App() {
  return (
    <ThemeProvider theme={theme}>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<LoginForm />} />
            <Route path="/register" element={<RegisterForm />} />
           
              <Route
              path="/waiting-approval"
              element={
                <ProtectedRoute requiredRole={ROLES.PENDING}>
                    <WaitingApproval/>
                </ProtectedRoute>
              }
            />
              <Route
                path="/doctor-slots"
                element={
                  <ProtectedRoute requiredRole={ROLES.DOCTOR}>
                    <MainLayout>
                      <RecurringSlotsManager />
                    </MainLayout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/doctor-visits"
                element={
                  <ProtectedRoute requiredRole={ROLES.DOCTOR}>
                    <MainLayout>
                      <VisitList />
                    </MainLayout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/collab-visits"
                element={
                  <ProtectedRoute requiredRole={ROLES.COLLABORATOR}>
                    <MainLayout>
                      <CollabVisits />
                    </MainLayout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/collab-assigned-visits"
                element={
                  <ProtectedRoute requiredRole={ROLES.COLLABORATOR}>
                    <MainLayout>
                      <CollabAssignedVisits />
                    </MainLayout>
                  </ProtectedRoute>
                }
              />
                      <Route
          path="/pending-users"
          element={
            <ProtectedRoute requiredRole={ROLES.ADMIN}>
              <MainLayout>
                <PendingUsersList />
              </MainLayout>
            </ProtectedRoute>
          }
        />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <Dashboard />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            
            <Route
              path="/rh-users"
              element={
                <ProtectedRoute requiredRole={ROLES.ADMIN}>
                  <MainLayout>
                    <RHUserList />
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/medecin-users"
              element={
                <ProtectedRoute requiredRole={ROLES.ADMIN}>
                  <MainLayout>
                    <MedecinUserList />
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/collaborateur-users"
              element={
                <ProtectedRoute requiredRole={ROLES.ADMIN}>
                  <MainLayout>
                    <CollaborateurUserList />
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/rh-spontaneous-visits"
              element={
                <ProtectedRoute requiredRole={ROLES.RH}>
                  <MainLayout>
                    <SpontaneousVisitsDemands />
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            <Route path="/" element={<LoginForm />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;