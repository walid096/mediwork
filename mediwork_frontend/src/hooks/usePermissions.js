import { useAuth } from '../contexts/AuthContext';
import { hasPermission as checkPermission } from '../utils/permissions';

export const usePermissions = () => {
    const { user } = useAuth();

    const hasPermission = (permission) => {
        if (!user) return false;
        return checkPermission(user.role, permission);
    };

    const canAccess = (route) => {
        if (!user) return false;
        return canAccess(user.role, route);
    };

    return { hasPermission, canAccess, userRole: user?.role };
};