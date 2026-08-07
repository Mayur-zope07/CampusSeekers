package com.campusseekers.repository;

import com.campusseekers.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID>, JpaSpecificationExecutor<Wishlist> {
    Optional<Wishlist> findByStudentProfileIdAndCollegeId(UUID studentProfileId, UUID collegeId);
    Optional<Wishlist> findByStudentProfileIdAndCollegeIdAndIsDeletedFalse(UUID studentProfileId, UUID collegeId);
    List<Wishlist> findByStudentProfileIdAndIsDeletedFalse(UUID studentProfileId);
}
