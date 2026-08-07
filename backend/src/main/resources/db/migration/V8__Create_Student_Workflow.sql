-- V8__Create_Student_Workflow.sql
-- Create student workflow tables: wishlist, admission_tracker, admission_tracker_history and modify shortlists

CREATE TABLE wishlist (
    id UUID PRIMARY KEY,
    student_profile_id UUID NOT NULL,
    college_id UUID NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_wishlist_student FOREIGN KEY (student_profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_college FOREIGN KEY (college_id) REFERENCES colleges(id) ON DELETE CASCADE,
    CONSTRAINT uq_wishlist_student_college UNIQUE (student_profile_id, college_id)
);

-- Modify shortlists to support priorities, personal notes, soft deletes, and optimistic locking
ALTER TABLE shortlists ADD COLUMN IF NOT EXISTS priority INTEGER;
ALTER TABLE shortlists ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE shortlists ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE shortlists ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;

CREATE TABLE admission_tracker (
    id UUID PRIMARY KEY,
    shortlist_id UUID NOT NULL UNIQUE,
    current_status VARCHAR(50) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_admission_tracker_shortlist FOREIGN KEY (shortlist_id) REFERENCES shortlists(id) ON DELETE CASCADE
);

CREATE TABLE admission_tracker_history (
    id UUID PRIMARY KEY,
    tracker_id UUID NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    remarks TEXT,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_tracker_history_tracker FOREIGN KEY (tracker_id) REFERENCES admission_tracker(id) ON DELETE CASCADE
);

CREATE INDEX idx_wishlist_student ON wishlist(student_profile_id);
CREATE INDEX idx_wishlist_college ON wishlist(college_id);
CREATE INDEX idx_admission_tracker_shortlist ON admission_tracker(shortlist_id);
CREATE INDEX idx_tracker_history_tracker ON admission_tracker_history(tracker_id);
