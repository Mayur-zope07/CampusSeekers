-- V5__Schema_Alignment.sql
-- Alter columns in colleges and college_branches tables to make them nullable

-- Colleges table
ALTER TABLE colleges ALTER COLUMN college_type DROP NOT NULL;
ALTER TABLE colleges ALTER COLUMN establishment_year DROP NOT NULL;
ALTER TABLE colleges ALTER COLUMN city DROP NOT NULL;
ALTER TABLE colleges ALTER COLUMN state DROP NOT NULL;

-- College Branches table
ALTER TABLE college_branches ALTER COLUMN fees_per_year DROP NOT NULL;
ALTER TABLE college_branches ALTER COLUMN duration_years DROP NOT NULL;
