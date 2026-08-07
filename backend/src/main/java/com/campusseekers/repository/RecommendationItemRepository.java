package com.campusseekers.repository;

import com.campusseekers.entity.RecommendationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, UUID> {
}
