-- V4__Add_Status_To_Colleges.sql
-- Add status column to colleges table

ALTER TABLE colleges ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
