-- V7__Create_Recommendation_Schema.sql
-- Create recommendations and recommendation_items tables for Smart College Recommendation Engine

CREATE TABLE recommendations (
    id UUID PRIMARY KEY,
    student_profile_id UUID NOT NULL,
    exam_name VARCHAR(50) NOT NULL,
    admission_year INTEGER NOT NULL,
    percentile NUMERIC(5, 2) NOT NULL,
    rank INTEGER,
    category VARCHAR(50) NOT NULL,
    preferred_branches TEXT,
    preferred_cities TEXT,
    preferred_college_types TEXT,
    minimum_naac VARCHAR(10),
    maximum_fees NUMERIC(12, 2),
    execution_time_ms INTEGER NOT NULL,
    evaluated_count INTEGER NOT NULL,
    filtered_count INTEGER NOT NULL,
    returned_count INTEGER NOT NULL,
    safe_count INTEGER NOT NULL,
    target_count INTEGER NOT NULL,
    dream_count INTEGER NOT NULL,
    engine_version VARCHAR(50) NOT NULL,
    algorithm_version VARCHAR(50) NOT NULL,
    safe_threshold NUMERIC(5, 2) NOT NULL,
    target_threshold NUMERIC(5, 2) NOT NULL,
    dream_threshold NUMERIC(5, 2) NOT NULL,
    cache_hit BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_recommendations_student FOREIGN KEY (student_profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE
);

CREATE TABLE recommendation_items (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL,
    college_branch_id UUID NOT NULL,
    closing_percentile NUMERIC(5, 2) NOT NULL,
    student_percentile NUMERIC(5, 2) NOT NULL,
    percentile_difference NUMERIC(5, 2) NOT NULL,
    recommendation_category VARCHAR(50) NOT NULL,
    recommendation_reason_code VARCHAR(100) NOT NULL,
    human_readable_reason VARCHAR(255) NOT NULL,
    CONSTRAINT fk_rec_items_recommendation FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_items_college_branch FOREIGN KEY (college_branch_id) REFERENCES college_branches(id) ON DELETE CASCADE
);

CREATE INDEX idx_recommendations_student_profile ON recommendations(student_profile_id);
CREATE INDEX idx_recommendation_items_recommendation ON recommendation_items(recommendation_id);
CREATE INDEX idx_recommendation_items_college_branch ON recommendation_items(college_branch_id);
