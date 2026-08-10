"use client";

import React from "react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { CheckSquare, Square } from "lucide-react";

interface TasksPanelProps {
    hasProfile: boolean;
    hasScores: boolean;
    hasRecommendations: boolean;
    hasWishlist: boolean;
}

export function TasksPanel({
    hasProfile = false,
    hasScores = false,
    hasRecommendations = false,
    hasWishlist = false,
}: TasksPanelProps) {
    const tasks = [
        { label: "Synchronize basic details", completed: hasProfile, tip: "Complete profile onboarding details" },
        { label: "Register entrance exam marks", completed: hasScores, tip: "Add your MHT-CET or JEE percentiles" },
        { label: "Review matched recommendations", completed: hasRecommendations, tip: "Explore colleges classified by cutoffs" },
        { label: "Save favorite colleges in wishlist", completed: hasWishlist, tip: "Add institutions to your target list" },
    ];

    return (
        <Card className="flex flex-col gap-4 p-5 h-full" hoverLift={false} glowColor="rgba(138, 43, 226, 0.05)">
            <div className="flex flex-col gap-0.5 select-none text-left">
                <Badge variant="purple" className="w-fit py-0 px-1.5 text-[8.5px]">Tasks Log</Badge>
                <h3 className="text-sm font-bold text-white mt-1">Setup Actions</h3>
            </div>
            <div className="flex flex-col gap-3">
                {tasks.map((task, idx) => (
                    <div key={idx} className="flex items-start gap-3 text-xs select-none">
                        <div className="mt-0.5 shrink-0">
                            {task.completed ? (
                                <CheckSquare className="w-4 h-4 text-accent-green" />
                            ) : (
                                <Square className="w-4 h-4 text-text-tertiary" />
                            )}
                        </div>
                        <div className="flex flex-col gap-0.5 text-left">
                            <span className={`font-semibold ${task.completed ? "text-text-disabled line-through" : "text-white"}`}>
                                {task.label}
                            </span>
                            <span className="text-[10px] text-text-tertiary">{task.tip}</span>
                        </div>
                    </div>
                ))}
            </div>
        </Card>
    );
}
