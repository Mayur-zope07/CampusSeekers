package com.campusseekers.repository;

import com.campusseekers.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollegeRepository extends JpaRepository<College, UUID> {
    Optional<College> findByCollegeCode(String collegeCode);
}
