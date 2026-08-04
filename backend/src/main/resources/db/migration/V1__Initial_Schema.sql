-- V1__Initial_Schema.sql
-- Base schema setup for CampusSeekers foundation

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index on email to optimize authentication lookups
CREATE INDEX idx_users_email ON users(email);
