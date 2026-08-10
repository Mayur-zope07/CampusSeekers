"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";

interface RoleGuardProps {
    children: React.ReactNode;
    allowedRoles: Array<"STUDENT" | "ADMIN">;
}

export function RoleGuard({ children, allowedRoles }: RoleGuardProps) {
    const { user, isLoading, isAuthenticated } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading) {
            if (!isAuthenticated) {
                router.push("/app/login");
            } else if (user && !allowedRoles.includes(user.role)) {
                router.push("/app/dashboard");
            }
        }
    }, [user, isAuthenticated, isLoading, allowedRoles, router]);

    if (isLoading || !isAuthenticated || (user && !allowedRoles.includes(user.role))) {
        return (
            <div className="min-h-screen bg-primary-bg flex flex-col items-center justify-center p-6 gap-4">
                <div className="w-10 h-10 rounded-full border-t-2 border-accent-orange animate-spin" />
                <span className="text-xs text-text-secondary select-none animate-pulse">
                    Verifying authorization permissions...
                </span>
            </div>
        );
    }

    return <>{children}</>;
}
