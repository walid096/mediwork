-- Initial data for the application
-- This will be executed automatically when the app starts

-- Admin user (default admin for any new installation)

INSERT INTO users (
    first_name,
    last_name,
    email,
    password,
    matricule,
    role,
    archived,
    failed_login_attempts,
    account_locked_until,
    date_creation,
    last_login
) VALUES (
             'Admin',
             'User',
             'admin@example.com',
             '$2a$10$rP3J/vZyH7VTPKJkOqruQubLwVjBhvMoZgV7/zrrcHQf7QmgQyl6G',
             'SQLI001',
             'ADMIN',
             false,
             0,
             NULL,
             CURRENT_TIMESTAMP,
             NULL
         ) ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    password = VALUES(password),
    role = VALUES(role),
    archived = VALUES(archived),
    failed_login_attempts = VALUES(failed_login_attempts),
    account_locked_until = VALUES(account_locked_until),
    date_creation = VALUES(date_creation),
    last_login = VALUES(last_login);

-- Sample collaborator user (optional - for testing)
INSERT INTO users (
    first_name,
    last_name,
    email,
    password,
    matricule,
    role,
    archived,
    failed_login_attempts,
    account_locked_until,
    date_creation,
    last_login
) VALUES (
             'John',
             'Doe',
             'johndoe@example.com',
             '$2a$10$PGfrOa5qxxIs.Y9LTphzmeThL2W9dZEF2OXZfSmfyt3eONLLh6.C2',
             'SQLI002',
             'COLLABORATEUR',
             false,
             0,
             NULL,
             CURRENT_TIMESTAMP,
             NULL
         ) ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    password = VALUES(password),
    role = VALUES(role),
    archived = VALUES(archived),
    failed_login_attempts = VALUES(failed_login_attempts),
    account_locked_until = VALUES(account_locked_until),
    date_creation = VALUES(date_creation),
    last_login = VALUES(last_login);
