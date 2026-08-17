import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/axios";

// ─── Types ────────────────────────────────────────────────────────────────────

export interface WishlistItem {
    id: string;
    studentProfileId: string;
    collegeId: string;
    collegeCode: string;
    collegeName: string;
    city: string;
    state: string;
    naacGrade: string;
    createdAt: string;
    isDeleted: boolean;
}

export interface TrackerItem {
    id: string;
    shortlistId: string;
    currentStatus: AdmissionStatus;
    remarks: string;
    createdAt: string;
    updatedAt: string;
}

export interface TrackerHistoryItem {
    id: string;
    trackerId: string;
    previousStatus: AdmissionStatus;
    newStatus: AdmissionStatus;
    remarks: string;
    changedAt: string;
}

export interface ShortlistItem {
    id: string;
    studentProfileId: string;
    collegeBranchId: string;
    collegeId: string;
    collegeCode: string;
    collegeName: string;
    branchId: string;
    branchCode: string;
    branchName: string;
    city: string;
    state: string;
    naacGrade: string;
    feesPerYear: number;
    priority: number;
    notes: string;
    isDeleted: boolean;
    addedAt: string;
    tracker: TrackerItem | null;
}

export type AdmissionStatus =
    | "INTERESTED"
    | "APPLIED"
    | "DOCUMENTS_UPLOADED"
    | "DOCUMENTS_VERIFIED"
    | "SEAT_ALLOTTED"
    | "CONFIRMED"
    | "REJECTED"
    | "WITHDRAWN";

// ─── Wishlist Hooks ───────────────────────────────────────────────────────────

export function useWishlist(params?: { keyword?: string; naac?: string }) {
    return useQuery({
        queryKey: ["wishlist", params],
        queryFn: async () => {
            const res = await api.get("/api/wishlist", { params: { ...params, size: 50 } });
            return res.data.data.content as WishlistItem[];
        },
    });
}

export function useCreateWishlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (collegeId: string) => {
            const res = await api.post("/api/wishlist", { collegeId });
            return res.data.data as WishlistItem;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["wishlist"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

export function useDeleteWishlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/api/wishlist/${id}`);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["wishlist"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

export function useRestoreWishlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            const res = await api.put(`/api/wishlist/${id}/restore`);
            return res.data.data as WishlistItem;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["wishlist"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

// ─── Shortlist Hooks ──────────────────────────────────────────────────────────

export function useShortlists() {
    return useQuery({
        queryKey: ["shortlists"],
        queryFn: async () => {
            const res = await api.get("/api/shortlists", { params: { size: 50 } });
            return res.data.data.content as ShortlistItem[];
        },
    });
}

export function useCreateShortlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (payload: { collegeBranchId: string; priority?: number; notes?: string }) => {
            const res = await api.post("/api/shortlists", payload);
            return res.data.data as ShortlistItem;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["shortlists"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

export function useUpdateShortlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async ({ id, priority, notes }: { id: string; priority?: number; notes?: string }) => {
            const res = await api.put(`/api/shortlists/${id}`, null, { params: { priority, notes } });
            return res.data.data as ShortlistItem;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["shortlists"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

export function useDeleteShortlist() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/api/shortlists/${id}`);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["shortlists"] });
            qc.invalidateQueries({ queryKey: ["admissionTracker"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
            qc.invalidateQueries({ queryKey: ["dashboardStatistics"] });
        },
    });
}

// ─── Admission Tracker Hooks ──────────────────────────────────────────────────

export function useAdmissionTracker() {
    return useQuery({
        queryKey: ["admissionTracker"],
        queryFn: async () => {
            const res = await api.get("/api/admission-tracker");
            return res.data.data as TrackerItem[];
        },
    });
}

export function useTrackerHistory(trackerId?: string) {
    return useQuery({
        queryKey: ["trackerHistory", trackerId],
        queryFn: async () => {
            const res = await api.get(`/api/admission-tracker/${trackerId}/history`);
            return res.data.data as TrackerHistoryItem[];
        },
        enabled: !!trackerId,
    });
}

export function useUpdateTracker() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async ({ id, status, remarks }: { id: string; status: AdmissionStatus; remarks?: string }) => {
            const res = await api.put(`/api/admission-tracker/${id}`, { status, remarks });
            return res.data.data as TrackerItem;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["admissionTracker"] });
            qc.invalidateQueries({ queryKey: ["shortlists"] });
            qc.invalidateQueries({ queryKey: ["dashboardData"] });
        },
    });
}

// ─── Export Hooks ─────────────────────────────────────────────────────────────

export function useExportPdf() {
    return useMutation({
        mutationFn: async (type: "dashboard" | "wishlist" | "shortlist" | "recommendations") => {
            const res = await api.get("/api/dashboard/export/pdf", { params: { type }, responseType: "blob" });
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const a = document.createElement("a");
            a.href = url;
            a.setAttribute("download", `campusseekers_${type}.pdf`);
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        },
    });
}

export function useExportCsv() {
    return useMutation({
        mutationFn: async (type: "dashboard" | "wishlist" | "shortlist" | "recommendations") => {
            const res = await api.get("/api/dashboard/export/csv", { params: { type }, responseType: "blob" });
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const a = document.createElement("a");
            a.href = url;
            a.setAttribute("download", `campusseekers_${type}.csv`);
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        },
    });
}
