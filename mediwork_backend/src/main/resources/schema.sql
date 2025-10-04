-- Database schema for the US-RH-01 project
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    matricule VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    archived BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
    );

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              user_id BIGINT NOT NULL,
                                              token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    device_info VARCHAR(255),
    last_used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user (user_id),
    INDEX idx_expiry (expiry_date),
    INDEX idx_user_revoked (user_id, revoked)
    );

-- Logs table (updated: action_type widened to 64)
CREATE TABLE IF NOT EXISTS logs (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    performed_by VARCHAR(255),
    action_type VARCHAR(64) NOT NULL,
    role VARCHAR(50),
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- ==================== MEDICAL VISIT SCHEDULING TABLES ====================

-- Slots table for medical appointment time slots
-- Manages doctor availability and prevents double-booking
CREATE TABLE IF NOT EXISTS slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_doctor (doctor_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_doctor_status (doctor_id, status),
    INDEX idx_start_end_time (start_time, end_time)
);

-- Visits table for medical visit records
-- Core table for US-RH-01: Schedule a Medical Visit
CREATE TABLE IF NOT EXISTS visits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    collaborator_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot_id BIGINT NULL,
    visit_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (collaborator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_collaborator (collaborator_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_slot (slot_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at),
    INDEX idx_visit_type (visit_type),
    INDEX idx_collaborator_status (collaborator_id, status),
    INDEX idx_doctor_status (doctor_id, status)
);
-- Recurring slots table for recurring doctor availability
CREATE TABLE IF NOT EXISTS recurring_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_recurring_doctor (doctor_id),
    INDEX idx_recurring_day (day_of_week),
    INDEX idx_recurring_doctor_day (doctor_id, day_of_week)
);

-- Spontaneous visit details table for storing additional information
-- about spontaneous visit requests from collaborators
CREATE TABLE IF NOT EXISTS spontaneous_visit_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    collaborator_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    additional_notes TEXT,
    preferred_date_time TIMESTAMP NULL,
    scheduling_status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (collaborator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_collaborator (collaborator_id),
    INDEX idx_reason (reason(255)),
    INDEX idx_preferred_date_time (preferred_date_time),
    INDEX idx_scheduling_status (scheduling_status)
);
