"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";

export function ProtectedRoute({ children, role }: { children: React.ReactNode; role?: "STUDENT" | "ADMIN" }) {
    const { isAuthenticated, isLoading, user } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            router.push("/app/login");
        } else if (!isLoading && isAuthenticated && role && user?.role !== role) {
            router.push("/app/dashboard");
        }
    }, [isAuthenticated, isLoading, router, role, user]);

    if (isLoading || !isAuthenticated || (role && user?.role !== role)) {
        return (
            <div className="min-h-screen bg-primary-bg flex flex-col items-center justify-center p-6 gap-4">
                <div className="w-10 h-10 rounded-full border-t-2 border-accent-cyan animate-spin" />
                <span className="text-xs text-text-secondary select-none animate-pulse">
                    Validating credentials session...
                </span>
            </div>
        );
    }

    return <>{children}</>;
}
