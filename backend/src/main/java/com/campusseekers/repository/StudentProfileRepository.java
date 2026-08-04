package com.campusseekers.repository;

import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
    Optional<StudentProfile> findByUserId(UUID userId);
    Optional<StudentProfile> findByUser(User user);
    boolean existsByUser(User user);
}
