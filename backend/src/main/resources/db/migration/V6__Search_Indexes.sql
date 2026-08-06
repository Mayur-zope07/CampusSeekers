-- V6__Search_Indexes.sql
-- Create indexes on commonly searched columns and foreign keys for search performance optimization

CREATE INDEX idx_colleges_search ON colleges(name, city, state);
CREATE INDEX idx_colleges_city ON colleges(city);
CREATE INDEX idx_colleges_state ON colleges(state);
CREATE INDEX idx_colleges_type ON colleges(college_type);
CREATE INDEX idx_colleges_naac ON colleges(naac_grade);
CREATE INDEX idx_colleges_status ON colleges(status);

CREATE INDEX idx_cutoffs_exam_year_round ON cutoffs(exam_name, year, round);
CREATE INDEX idx_cutoffs_category ON cutoffs(category);
CREATE INDEX idx_cutoffs_raw_seat_type ON cutoffs(seat_type);

-- Indexes on foreign keys to optimize relational join scans
CREATE INDEX idx_college_branches_college_fk ON college_branches(college_id);
CREATE INDEX idx_college_branches_branch_fk ON college_branches(branch_id);
CREATE INDEX idx_cutoffs_college_branch_fk ON cutoffs(college_branch_id);
CREATE INDEX idx_placements_college_fk ON placements(college_id);
