"use client";

import React from "react";
import { Badge } from "@/components/ui/Badge";
import { Sparkles } from "lucide-react";

interface WelcomeHeaderProps {
    firstName?: string;
    lastName?: string;
    percentile?: number;
    examName?: string;
}

export function WelcomeHeader({ firstName = "Student", lastName = "", percentile = 0, examName = "MHT_CET" }: WelcomeHeaderProps) {
    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    };

    return (
        <div className="flex flex-col gap-2 select-none text-left">
            <div className="flex flex-wrap items-center gap-2">
                <Badge variant="purple" glow className="px-2.5 py-0.5 text-[10px]">
                    <Sparkles className="w-3 h-3 mr-1 inline text-accent-cyan animate-pulse" />
                    Admission Workspace Active
                </Badge>
            </div>
            <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight mt-1">
                {getGreeting()},{" "}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan to-accent-purple">
                    {firstName} {lastName}
                </span>
            </h1>
            <p className="text-xs text-text-secondary max-w-xl leading-relaxed">
                Currently optimizing matching relative to your <span className="text-white font-semibold">{examName.replace("_", " ")}</span> percentile score of <span className="text-accent-cyan font-bold">{percentile}%</span>.
            </p>
        </div>
    );
}
