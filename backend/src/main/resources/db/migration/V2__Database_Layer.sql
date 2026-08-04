-- V2__Database_Layer.sql
-- Create student_profiles, exam_scores, colleges, branches, college_branches, cutoffs, placements, and shortlists

-- 1. Student Profiles
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    gender VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(50),
    home_state VARCHAR(100) NOT NULL,
    home_district VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_profiles_user ON student_profiles(user_id);

-- 2. Exam Scores
CREATE TABLE exam_scores (
    id UUID PRIMARY KEY,
    student_profile_id UUID NOT NULL,
    exam_name VARCHAR(50) NOT NULL,
    score_percentile NUMERIC(5, 2) NOT NULL,
    score_rank INTEGER NOT NULL,
    exam_year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_exam_scores_profile FOREIGN KEY (student_profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_exam_scores_profile_exam UNIQUE (student_profile_id, exam_name, exam_year)
);

CREATE INDEX idx_exam_scores_profile ON exam_scores(student_profile_id);

-- 3. Colleges
CREATE TABLE colleges (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    college_code VARCHAR(50) NOT NULL UNIQUE,
    college_type VARCHAR(50) NOT NULL,
    establishment_year INTEGER NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    website VARCHAR(255),
    naac_grade VARCHAR(10),
    nba_accredited BOOLEAN NOT NULL DEFAULT FALSE,
    campus_size VARCHAR(50),
    logo_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_colleges_code ON colleges(college_code);

-- 4. Branches
CREATE TABLE branches (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    branch_code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_branches_code ON branches(branch_code);

-- 5. College Branches (Junction)
CREATE TABLE college_branches (
    id UUID PRIMARY KEY,
    college_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    intake_capacity INTEGER NOT NULL,
    fees_per_year NUMERIC(12, 2) NOT NULL,
    duration_years INTEGER NOT NULL DEFAULT 4,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_college_branches_college FOREIGN KEY (college_id) REFERENCES colleges(id) ON DELETE CASCADE,
    CONSTRAINT fk_college_branches_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT uq_college_branches_college_branch UNIQUE (college_id, branch_id)
);

CREATE INDEX idx_college_branches_college ON college_branches(college_id);
CREATE INDEX idx_college_branches_branch ON college_branches(branch_id);

-- 6. Cutoffs
CREATE TABLE cutoffs (
    id UUID PRIMARY KEY,
    college_branch_id UUID NOT NULL,
    exam_name VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    round INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    closing_rank INTEGER NOT NULL,
    closing_percentile NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cutoffs_college_branch FOREIGN KEY (college_branch_id) REFERENCES college_branches(id) ON DELETE CASCADE,
    CONSTRAINT uq_cutoffs_combination UNIQUE (college_branch_id, exam_name, year, round, category, seat_type)
);

CREATE INDEX idx_cutoffs_college_branch ON cutoffs(college_branch_id);
CREATE INDEX idx_cutoffs_closing_rank ON cutoffs(closing_rank);
CREATE INDEX idx_cutoffs_closing_percentile ON cutoffs(closing_percentile);

-- 7. Placements
CREATE TABLE placements (
    id UUID PRIMARY KEY,
    college_id UUID NOT NULL,
    year INTEGER NOT NULL,
    average_package NUMERIC(10, 2) NOT NULL,
    highest_package NUMERIC(10, 2) NOT NULL,
    placement_ratio NUMERIC(5, 2) NOT NULL,
    top_recruiters TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_placements_college FOREIGN KEY (college_id) REFERENCES colleges(id) ON DELETE CASCADE,
    CONSTRAINT uq_placements_college_year UNIQUE (college_id, year)
);

CREATE INDEX idx_placements_college ON placements(college_id);

-- 8. Shortlists
CREATE TABLE shortlists (
    id UUID PRIMARY KEY,
    student_profile_id UUID NOT NULL,
    college_branch_id UUID NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_shortlists_profile FOREIGN KEY (student_profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_shortlists_college_branch FOREIGN KEY (college_branch_id) REFERENCES college_branches(id) ON DELETE CASCADE,
    CONSTRAINT uq_shortlists_profile_branch UNIQUE (student_profile_id, college_branch_id)
);

CREATE INDEX idx_shortlists_profile ON shortlists(student_profile_id);
CREATE INDEX idx_shortlists_college_branch ON shortlists(college_branch_id);
