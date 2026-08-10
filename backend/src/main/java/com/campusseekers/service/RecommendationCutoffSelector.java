package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.entity.Cutoff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationCutoffSelector {

    private final RecommendationProperties properties;

    private static final Map<String, Integer> STAGE_PRIORITY = new HashMap<>();
    static {
        STAGE_PRIORITY.put("MH", 100);
        STAGE_PRIORITY.put("VII", 70);
        STAGE_PRIORITY.put("VI", 60);
        STAGE_PRIORITY.put("V", 50);
        STAGE_PRIORITY.put("IV", 40);
        STAGE_PRIORITY.put("III", 30);
        STAGE_PRIORITY.put("II", 20);
        STAGE_PRIORITY.put("I-NON PWD", 12);
        STAGE_PRIORITY.put("I-NON-PWD", 12);
        STAGE_PRIORITY.put("I-NON DEFENCE", 11);
        STAGE_PRIORITY.put("I-NON-DEFENCE", 11);
        STAGE_PRIORITY.put("I", 10);
    }

    public List<Cutoff> selectRepresentativeCutoffs(List<Cutoff> cutoffs) {
        if (cutoffs == null || cutoffs.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by (college_branch_id, round, category, seat_type)
        Map<String, List<Cutoff>> grouped = cutoffs.stream()
                .collect(Collectors.groupingBy(c -> 
                        c.getCollegeBranch().getId().toString() + "|" +
                        c.getRound() + "|" +
                        c.getCategory().name() + "|" +
                        c.getRawSeatType().trim().toUpperCase()
                ));

        List<Cutoff> representatives = new ArrayList<>();
        for (Map.Entry<String, List<Cutoff>> entry : grouped.entrySet()) {
            List<Cutoff> group = entry.getValue();
            if (group.size() == 1) {
                representatives.add(group.get(0));
            } else {
                Cutoff selected = selectBestCutoff(group);
                representatives.add(selected);
            }
        }

        log.debug("Selected {} representative cutoffs from {} raw cutoffs", representatives.size(), cutoffs.size());
        return representatives;
    }

    private Cutoff selectBestCutoff(List<Cutoff> group) {
        // Sort by stage priority descending (latest stage first)
        return group.stream().min((c1, c2) -> {
            int p1 = getStagePriority(c1.getStage());
            int p2 = getStagePriority(c2.getStage());

            if (p1 != p2) {
                // Higher priority first
                return Integer.compare(p2, p1);
            }

            // Fallback selection policy
            int pctComp = c2.getClosingPercentile().compareTo(c1.getClosingPercentile());
            if (pctComp != 0) {
                if ("LOWEST_PERCENTILE".equalsIgnoreCase(properties.getFallbackSelectionPolicy())) {
                    return -pctComp; // Prefer lower percentile (higher rank)
                }
                return pctComp; // Default: prefer higher percentile (lower rank / harder admission)
            }

            // Tie-breaker
            return c1.getStage().compareTo(c2.getStage());
        }).orElse(group.get(0));
    }

    private int getStagePriority(String stage) {
        if (stage == null) {
            return 0;
        }
        String key = stage.trim().toUpperCase().replace("\n", " ").replaceAll("\\s+", " ");
        
        // Strip section suffixes
        if (key.endsWith("-HU")) {
            key = key.substring(0, key.length() - 3);
        } else if (key.endsWith("-OHU")) {
            key = key.substring(0, key.length() - 4);
        } else if (key.endsWith("-SL")) {
            key = key.substring(0, key.length() - 3);
        }
        
        return STAGE_PRIORITY.getOrDefault(key, 0);
    }
}
