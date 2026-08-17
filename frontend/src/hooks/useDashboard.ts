import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/axios";

export function useDashboardData() {
    return useQuery({
        queryKey: ["dashboardData"],
        queryFn: async () => {
            const res = await api.get("/api/dashboard");
            return res.data.data;
        },
    });
}

export function useDashboardStatistics() {
    return useQuery({
        queryKey: ["dashboardStatistics"],
        queryFn: async () => {
            const res = await api.get("/api/dashboard/statistics");
            return res.data.data;
        },
    });
}

export function useProfile() {
    return useQuery({
        queryKey: ["studentProfile"],
        queryFn: async () => {
            const res = await api.get("/api/profile");
            return res.data.data;
        },
    });
}

export function useStudentScores() {
    return useQuery({
        queryKey: ["studentScores"],
        queryFn: async () => {
            const res = await api.get("/api/profile/scores");
            return res.data.data;
        },
    });
}

