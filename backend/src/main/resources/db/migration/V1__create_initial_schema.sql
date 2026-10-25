-- V1: Initial Database Schema for CrimeScope AI
-- Normalized schema for Tamil Nadu Crime Intelligence Command Center

CREATE TABLE IF NOT EXISTS districts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    headquarters VARCHAR(100) NOT NULL,
    zone VARCHAR(50) NOT NULL,
    area_sq_km DECIMAL(10, 2),
    population_2021_est BIGINT,
    center_lat DECIMAL(9, 6),
    center_lng DECIMAL(9, 6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_districts_zone ON districts(zone);
CREATE INDEX idx_districts_name ON districts(name);

CREATE TABLE IF NOT EXISTS crime_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    severity_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_ipc BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    badge_number VARCHAR(50),
    department VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dataset_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_name VARCHAR(150) NOT NULL,
    agency VARCHAR(150) NOT NULL,
    publication_title VARCHAR(255) NOT NULL,
    reporting_year_start INT NOT NULL,
    reporting_year_end INT NOT NULL,
    license VARCHAR(100) NOT NULL DEFAULT 'Government Open Data License - India (GODL)',
    source_url VARCHAR(500),
    is_demonstration BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dataset_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT,
    imported_by_user_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_checksum VARCHAR(64),
    total_rows_read INT NOT NULL DEFAULT 0,
    valid_records_imported INT NOT NULL DEFAULT 0,
    malformed_rows_count INT NOT NULL DEFAULT 0,
    duplicate_rows_skipped INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    execution_time_ms BIGINT NOT NULL DEFAULT 0,
    log_summary TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_imports_source FOREIGN KEY (source_id) REFERENCES dataset_sources(id) ON DELETE SET NULL,
    CONSTRAINT fk_imports_user FOREIGN KEY (imported_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS crime_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    source_id BIGINT,
    import_id BIGINT,
    reporting_year INT NOT NULL,
    reporting_month INT,
    case_count INT NOT NULL DEFAULT 0,
    arrest_count INT,
    convicted_count INT,
    chargesheeted_count INT,
    is_demonstration BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'VERIFIED',
    reporting_unit VARCHAR(100) DEFAULT 'District Police Office',
    incident_date DATE,
    latitude DECIMAL(9, 6),
    longitude DECIMAL(9, 6),
    location_description VARCHAR(255),
    source_reference VARCHAR(150),
    data_hash VARCHAR(64) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_records_category FOREIGN KEY (category_id) REFERENCES crime_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_records_source FOREIGN KEY (source_id) REFERENCES dataset_sources(id) ON DELETE SET NULL,
    CONSTRAINT fk_records_import FOREIGN KEY (import_id) REFERENCES dataset_imports(id) ON DELETE SET NULL
);

CREATE INDEX idx_records_dist_year ON crime_records(district_id, reporting_year);
CREATE INDEX idx_records_cat_year ON crime_records(category_id, reporting_year);
CREATE INDEX idx_records_year_month ON crime_records(reporting_year, reporting_month);
CREATE INDEX idx_records_status ON crime_records(verification_status, is_demonstration);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_action ON audit_logs(action, created_at);
