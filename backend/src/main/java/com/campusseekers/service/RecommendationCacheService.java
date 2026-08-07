package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Recommendation;
import com.campusseekers.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationCacheService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationProperties properties;

    public Optional<Recommendation> findCachedRecommendation(UUID studentProfileId, RecommendationRequest request) {
        log.info("Checking for cached recommendation for student: {}", studentProfileId);

        int cacheMinutes = properties.getCacheMinutes();
        if (cacheMinutes <= 0) {
            log.info("Caching is disabled (cacheMinutes = {})", cacheMinutes);
            return Optional.empty();
        }

        Instant since = Instant.now().minus(cacheMinutes, ChronoUnit.MINUTES);
        List<Recommendation> recentRecommendations = recommendationRepository
                .findByStudentProfileIdAndCreatedAtAfterOrderByCreatedAtDesc(studentProfileId, since);

        for (Recommendation rec : recentRecommendations) {
            if (isIdenticalRequest(rec, request)) {
                log.info("Cache HIT found recommendation ID: {}", rec.getId());
                return Optional.of(rec);
            }
        }

        log.info("Cache MISS for student: {}", studentProfileId);
        return Optional.empty();
    }

    private boolean isIdenticalRequest(Recommendation r, RecommendationRequest req) {
        return r.getExamName() == req.exam() &&
                r.getAdmissionYear().equals(req.year()) &&
                r.getPercentile().compareTo(req.percentile()) == 0 &&
                Objects.equals(r.getRank(), req.rank()) &&
                r.getCategory() == req.category() &&
                listEqualsIgnoreOrder(r.getPreferredBranches(), req.preferredBranches()) &&
                listEqualsIgnoreOrder(r.getPreferredCities(), req.preferredCities()) &&
                listEqualsIgnoreOrder(r.getPreferredCollegeTypes(), req.preferredCollegeTypes()) &&
                Objects.equals(r.getMinimumNaac(), req.minimumNAAC()) &&
                (r.getMaximumFees() == null && req.maximumFees() == null ||
                 r.getMaximumFees() != null && req.maximumFees() != null && r.getMaximumFees().compareTo(req.maximumFees()) == 0);
    }

    private <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        return list1.containsAll(list2) && list2.containsAll(list1);
    }
}
