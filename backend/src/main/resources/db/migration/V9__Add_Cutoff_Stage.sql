-- V9__Add_Cutoff_Stage.sql
-- Add stage column to cutoffs and update unique combination constraint

ALTER TABLE cutoffs ADD COLUMN stage VARCHAR(50);

-- Populate existing records with a default stage of 'I'
UPDATE cutoffs SET stage = 'I' WHERE stage IS NULL;

-- Make stage column NOT NULL
ALTER TABLE cutoffs ALTER COLUMN stage SET NOT NULL;

-- Drop the old unique constraint
ALTER TABLE cutoffs DROP CONSTRAINT uq_cutoffs_combination;

-- Recreate unique constraint with stage included
ALTER TABLE cutoffs ADD CONSTRAINT uq_cutoffs_combination UNIQUE (college_branch_id, exam_name, year, round, category, seat_type, stage);
