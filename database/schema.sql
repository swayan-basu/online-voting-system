CREATE DATABASE IF NOT EXISTS voting_system;
USE voting_system;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    voter_id VARCHAR(20) NOT NULL UNIQUE,
    identity_number VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'voter',
    has_voted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS candidates (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    party VARCHAR(120) NOT NULL,
    vote_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id INT NOT NULL,
    cast_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id)
);

CREATE TABLE IF NOT EXISTS election_settings (
    id INT PRIMARY KEY,
    status VARCHAR(10) NOT NULL DEFAULT 'CLOSED',
    end_time DATETIME NULL
);

INSERT IGNORE INTO election_settings (id, status, end_time) VALUES (1, 'CLOSED', NULL);

CREATE TABLE IF NOT EXISTS approved_voters (
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(30) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (document_type, document_number)
);

CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_key VARCHAR(512) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

