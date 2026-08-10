import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/axios";

export function useColleges(params: Record<string, unknown>) {
    return useQuery({
        queryKey: ["colleges", params],
        queryFn: async () => {
            const res = await api.get("/api/colleges", { params });
            return res.data.data;
        },
    });
}

export function useCollege(id?: string) {
    return useQuery({
        queryKey: ["collegeDetail", id],
        queryFn: async () => {
            if (!id) return null;
            const res = await api.get(`/api/colleges/${id}`);
            return res.data.data;
        },
        enabled: !!id,
    });
}

export function useBranches() {
    return useQuery({
        queryKey: ["branches"],
        queryFn: async () => {
            const res = await api.get("/api/branches");
            return res.data.data;
        },
    });
}

export function useCutoffs(params: Record<string, unknown>) {
    return useQuery({
        queryKey: ["cutoffs", params],
        queryFn: async () => {
            const res = await api.get("/api/cutoffs", { params });
            return res.data.data;
        },
    });
}

export function usePlacements(params: Record<string, unknown>) {
    return useQuery({
        queryKey: ["placements", params],
        queryFn: async () => {
            const res = await api.get("/api/placements", { params });
            return res.data.data;
        },
    });
}

export function useSearch(params: Record<string, unknown>, enabled = false) {
    return useQuery({
        queryKey: ["percentileSearch", params],
        queryFn: async () => {
            const res = await api.get("/api/search", { params });
            return res.data.data;
        },
        enabled,
    });
}

export function useComparison(collegeIds: string[], enabled = false) {
    return useQuery({
        queryKey: ["comparison", collegeIds],
        queryFn: async () => {
            const res = await api.get("/api/compare", {
                params: { collegeIds: collegeIds.join(",") },
            });
            return res.data.data;
        },
        enabled,
    });
}
