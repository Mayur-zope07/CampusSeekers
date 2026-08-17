import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/axios";

// ─── Types ────────────────────────────────────────────────────────────────────

export interface RecommendationRequestPayload {
    exam: string;
    year: number;
    percentile: number;
    rank?: number | null;
    category: string;
    preferredBranches?: string[];
    preferredCities?: string[];
    preferredCollegeTypes?: string[];
    minimumNAAC?: string | null;
    maximumFees?: number | null;
}

export interface RecommendationItem {
    id: string;
    collegeId: string;
    collegeCode: string;
    collegeName: string;
    branchId: string;
    branchCode: string;
    branchName: string;
    city: string;
    state: string;
    collegeType: string;
    naacGrade: string;
    nbaAccredited: boolean;
    durationYears: number;
    intakeCapacity: number;
    feesPerYear: number;
    closingPercentile: number;
    studentPercentile: number;
    percentileDifference: number;
    recommendationCategory: "SAFE" | "TARGET" | "DREAM";
    recommendationReasonCode: string;
    humanReadableReason: string;
    placementRatio: number;
    averagePackage: number;
    highestPackage: number;
}

export interface RecommendationResult {
    id: string;
    examName: string;
    admissionYear: number;
    percentile: number;
    rank: number;
    category: string;
    preferredBranches: string[];
    preferredCities: string[];
    preferredCollegeTypes: string[];
    minimumNaac: string;
    maximumFees: number;
    executionTimeMs: number;
    evaluatedCount: number;
    filteredCount: number;
    returnedCount: number;
    safeCount: number;
    targetCount: number;
    dreamCount: number;
    engineVersion: string;
    algorithmVersion: string;
    safeThreshold: number;
    targetThreshold: number;
    dreamThreshold: number;
    cacheHit: boolean;
    createdAt: string;
    items: RecommendationItem[];
}

export interface RecommendationHistory {
    id: string;
    examName: string;
    admissionYear: number;
    percentile: number;
    rank: number;
    category: string;
    preferredBranches: string[];
    preferredCities: string[];
    preferredCollegeTypes: string[];
    minimumNaac: string;
    maximumFees: number;
    executionTimeMs: number;
    returnedCount: number;
    cacheHit: boolean;
    createdAt: string;
}

// ─── Hooks ────────────────────────────────────────────────────────────────────

export function useGenerateRecommendation() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (payload: RecommendationRequestPayload) => {
            const res = await api.post("/api/recommendations", payload);
            return res.data.data as RecommendationResult;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["recommendationHistory"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
        },
    });
}

export function useRecommendationHistory() {
    return useQuery({
        queryKey: ["recommendationHistory"],
        queryFn: async () => {
            const res = await api.get("/api/recommendations/history", { params: { size: 20 } });
            return res.data.data.content as RecommendationHistory[];
        },
    });
}

export function useRecommendation(id?: string) {
    return useQuery({
        queryKey: ["recommendation", id],
        queryFn: async () => {
            const res = await api.get(`/api/recommendations/${id}`);
            return res.data.data as RecommendationResult;
        },
        enabled: !!id,
    });
}

export function useShortlistRecommendation() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (recommendationItemId: string) => {
            const res = await api.post(`/api/recommendations/${recommendationItemId}/shortlist`);
            return res.data.data;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
        },
    });
}
