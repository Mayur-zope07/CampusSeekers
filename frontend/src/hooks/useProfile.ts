import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/axios";

// ─── Types ────────────────────────────────────────────────────────────────────

export type Gender = "MALE" | "FEMALE" | "OTHER";
export type Category = "OPEN" | "OBC" | "SC" | "ST" | "EWS" | "NT1" | "NT2" | "NT3";
export type ExamName = "MHT_CET" | "JEE_MAIN" | "JEE_ADVANCED";
export type Role = "STUDENT" | "ADMIN";

export interface StudentProfile {
    id: string;
    userId: string;
    firstName: string;
    lastName: string;
    phone: string;
    gender: Gender;
    category: Category;
    subCategory: string;
    homeState: string;
    homeDistrict: string;
    createdAt: string;
    updatedAt: string;
}

export interface StudentProfilePayload {
    firstName: string;
    lastName: string;
    phone: string;
    gender: Gender;
    category: Category;
    subCategory?: string;
    homeState: string;
    homeDistrict: string;
}

export interface ExamScore {
    id: string;
    examName: ExamName;
    examYear: number;
    marks: number | null;
    rank: number;
    percentile: number;
    createdAt: string;
    updatedAt: string;
}

export interface ExamScorePayload {
    examName: ExamName;
    examYear: number;
    rank: number;
    percentile: number;
    marks?: number | null;
}

export interface CurrentUser {
    id: string;
    email: string;
    role: Role;
    createdAt: string;
}

// ─── Profile Hooks ────────────────────────────────────────────────────────────

export function useProfile() {
    return useQuery({
        queryKey: ["studentProfile"],
        queryFn: async () => {
            const res = await api.get("/api/profile");
            return res.data.data as StudentProfile;
        },
    });
}

export function useUpdateProfile() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (payload: StudentProfilePayload) => {
            const res = await api.put("/api/profile", payload);
            return res.data.data as StudentProfile;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["studentProfile"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
        },
    });
}

// ─── Exam Score Hooks ─────────────────────────────────────────────────────────

export function useScores() {
    return useQuery({
        queryKey: ["examScores"],
        queryFn: async () => {
            const res = await api.get("/api/profile/scores");
            return res.data.data as ExamScore[];
        },
    });
}

export function useCreateScore() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (payload: ExamScorePayload) => {
            const res = await api.post("/api/profile/scores", payload);
            return res.data.data as ExamScore;
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["examScores"] }),
    });
}

export function useUpdateScore() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async ({ id, payload }: { id: string; payload: ExamScorePayload }) => {
            const res = await api.put(`/api/profile/scores/${id}`, payload);
            return res.data.data as ExamScore;
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["examScores"] }),
    });
}

export function useDeleteScore() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/api/profile/scores/${id}`);
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["examScores"] }),
    });
}

// ─── Auth Hooks ───────────────────────────────────────────────────────────────

export function useCurrentUser() {
    return useQuery({
        queryKey: ["currentUser"],
        queryFn: async () => {
            const res = await api.get("/api/auth/me");
            return res.data as CurrentUser;
        },
    });
}

export function useLogout() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            // Backend has no logout endpoint; clear client state only
            localStorage.removeItem("access_token");
            localStorage.removeItem("refresh_token");
        },
        onSuccess: () => {
            qc.clear();
            window.location.href = "/app/login";
        },
    });
}
