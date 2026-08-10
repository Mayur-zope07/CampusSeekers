"use client";

import React from "react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { IndianRupee, Heart, Star, TrendingUp } from "lucide-react";

interface StatsSectionProps {
    wishlistCount?: number;
    shortlistCount?: number;
    safeCount?: number;
    targetCount?: number;
    dreamCount?: number;
    averageFees?: number;
    highestPackage?: number;
}

export function StatsSection({
    wishlistCount = 0,
    shortlistCount = 0,
    safeCount = 0,
    targetCount = 0,
    dreamCount = 0,
    averageFees = 0,
    highestPackage = 0,
}: StatsSectionProps) {
    const formatCurrency = (val: number) => {
        if (!val) return "0";
        if (val >= 100000) return `${(val / 100000).toFixed(2)} L`;
        return val.toLocaleString();
    };

    const stats = [
        { label: "Wishlisted", value: wishlistCount, icon: <Heart className="w-4 h-4 text-accent-orange" /> },
        { label: "Shortlisted", value: shortlistCount, icon: <Star className="w-4 h-4 text-accent-cyan" /> },
        { label: "Safe Options", value: safeCount, icon: <Badge variant="green" className="py-0 px-1 text-[9px]">SAFE</Badge> },
        { label: "Target Options", value: targetCount, icon: <Badge variant="cyan" className="py-0 px-1 text-[9px]">TARGET</Badge> },
        { label: "Dream Options", value: dreamCount, icon: <Badge variant="purple" className="py-0 px-1 text-[9px]">DREAM</Badge> },
        { label: "Avg Fees / Yr", value: `₹${formatCurrency(averageFees)}`, icon: <IndianRupee className="w-4 h-4 text-text-secondary" /> },
        { label: "Max Package", value: `₹${formatCurrency(highestPackage)}`, icon: <TrendingUp className="w-4 h-4 text-accent-green" /> },
    ];

    return (
        <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-7 gap-4 select-none">
            {stats.map((item, idx) => (
                <Card key={idx} className="flex flex-col gap-1 p-4 h-full" hoverLift={false} glowColor="rgba(0, 240, 255, 0.03)">
                    <div className="flex items-center justify-between w-full">
                        <span className="text-[9px] font-bold text-text-tertiary uppercase tracking-wider">
                            {item.label}
                        </span>
                        <div className="shrink-0">{item.icon}</div>
                    </div>
                    <span className="text-xl md:text-2xl font-light font-futuristic text-white mt-1">
                        {item.value}
                    </span>
                </Card>
            ))}
        </div>
    );
}
