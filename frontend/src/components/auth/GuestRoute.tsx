"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";

export function GuestRoute({ children }: { children: React.ReactNode }) {
    const { isAuthenticated, isLoading } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading && isAuthenticated) {
            router.push("/app/dashboard");
        }
    }, [isAuthenticated, isLoading, router]);

    if (isLoading) {
        return (
            <div className="min-h-screen bg-primary-bg flex flex-col items-center justify-center p-6 gap-4">
                <div className="w-10 h-10 rounded-full border-t-2 border-accent-purple animate-spin" />
                <span className="text-xs text-text-secondary select-none animate-pulse">
                    Loading account profile...
                </span>
            </div>
        );
    }

    if (isAuthenticated) {
        return null;
    }

    return <>{children}</>;
}
