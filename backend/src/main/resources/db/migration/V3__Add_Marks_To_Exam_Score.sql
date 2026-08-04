-- V3__Add_Marks_To_Exam_Score.sql
-- Add optional marks column to exam_scores table

ALTER TABLE exam_scores ADD COLUMN marks INTEGER;
