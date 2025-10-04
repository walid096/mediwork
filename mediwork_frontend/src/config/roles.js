// Define all user roles
export const ROLES = {
    ADMIN: 'ADMIN',
    RH: 'RH',
    DOCTOR: 'DOCTOR',
    COLLABORATOR: 'COLLABORATOR',
    PENDING : 'PENDING'

};

// Define role-based permissions
export const PERMISSIONS = {
    USER_MANAGEMENT: {
        CREATE: [ROLES.ADMIN],
        UPDATE: [ROLES.ADMIN],
        DELETE: [ROLES.ADMIN],
        RESTORE: [ROLES.ADMIN],
        VIEW: [ROLES.ADMIN]
    }
};

// Define role-based routes
export const ROLE_ROUTES = {
    [ROLES.ADMIN]: ['/dashboard', '/users', '/profile'],
    [ROLES.RH]: ['/dashboard', '/profile'],
    [ROLES.DOCTOR]: ['/dashboard', '/profile'],
    [ROLES.COLLABORATOR]: ['/dashboard', '/profile'],
    [ROLES.PENDING]: ['/waiting-approval']
};