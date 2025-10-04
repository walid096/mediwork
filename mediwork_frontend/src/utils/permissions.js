import { ROLES, PERMISSIONS, ROLE_ROUTES } from '../config/roles';

export const hasPermission = (userRole, permission) => {
    const permissionArray = permission.split('.');
    const module = permissionArray[0];
    const action = permissionArray[1];

    return PERMISSIONS[module]?.[action]?.includes(userRole) || false;
};

export const canAccess = (userRole, route) => {
    const allowedRoutes = ROLE_ROUTES[userRole] || [];
    return allowedRoutes.includes(route);
};

export const checkUserRole = (user, requiredRole) => {
    return user?.role === requiredRole;
};