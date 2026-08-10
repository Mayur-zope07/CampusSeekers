"use client";

import React from "react";
import { Card } from "../ui/Card";
import { Badge } from "../ui/Badge";
import { ScrollReveal } from "../animations/ScrollReveal";
import { CheckCircle2, AlertTriangle, HelpCircle } from "lucide-react";

export function RecommendationPreview() {
    const previews = [
        {
            category: "SAFE",
            badgeVariant: "green" as const,
            college: "COEP Technological University",
            branch: "Civil Engineering",
            cutoff: "96.20%",
            difference: "+2.30%",
            probability: "98%",
            description: "High chance of admission based on historical trends.",
            icon: <CheckCircle2 className="text-accent-green w-5 h-5 shrink-0" />,
            glow: "rgba(57, 255, 20, 0.12)",
        },
        {
            category: "TARGET",
            badgeVariant: "cyan" as const,
            college: "VJTI Mumbai",
            branch: "Electronics Engineering",
            cutoff: "98.40%",
            difference: "+0.10%",
            probability: "75%",
            description: "Excellent match; highly competitive but achievable.",
            icon: <HelpCircle className="text-accent-cyan w-5 h-5 shrink-0" />,
            glow: "rgba(0, 240, 255, 0.15)",
        },
        {
            category: "DREAM",
            badgeVariant: "purple" as const,
            college: "VJTI Mumbai",
            branch: "Computer Engineering",
            cutoff: "99.20%",
            difference: "-0.70%",
            probability: "35%",
            description: "Historical cutoffs are slightly higher than your score.",
            icon: <AlertTriangle className="text-accent-purple w-5 h-5 shrink-0" />,
            glow: "rgba(138, 43, 226, 0.15)",
        },
    ];

    return (
        <section id="recommendations" className="relative py-24 bg-primary-bg max-w-5xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="text-center mb-16 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-purple font-bold uppercase tracking-wider">Preview Engine</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        Dynamic Match Categories
                    </h2>
                    <p className="text-xs text-text-secondary max-w-lg leading-relaxed mt-1">
                        Our engine ranks your options into three categories matching historical cutoff safety margins.
                    </p>
                </div>
            </ScrollReveal>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                {previews.map((item, idx) => (
                    <ScrollReveal key={idx} delay={idx * 0.1}>
                        <Card className="flex flex-col gap-6 h-full justify-between" glowColor={item.glow}>
                            <div className="flex flex-col gap-4">
                                <div className="flex justify-between items-center">
                                    <Badge variant={item.badgeVariant} glow>
                                        {item.category}
                                    </Badge>
                                    <span className="text-xs font-semibold text-text-secondary select-none">
                                        Chance: {item.probability}
                                    </span>
                                </div>
                                <div className="flex flex-col gap-1.5">
                                    <h4 className="text-sm font-bold text-white leading-tight select-none">{item.college}</h4>
                                    <span className="text-xs text-text-tertiary select-none">{item.branch}</span>
                                </div>
                            </div>

                            <div className="flex flex-col gap-4 border-t border-border-color/30 pt-4 mt-2">
                                <div className="flex items-center justify-between text-xs">
                                    <span className="text-text-tertiary select-none">Closing Cutoff</span>
                                    <span className="font-semibold text-white">{item.cutoff}</span>
                                </div>
                                <div className="flex items-center justify-between text-xs">
                                    <span className="text-text-tertiary select-none">Your Difference</span>
                                    <span className={`font-semibold ${item.category === "DREAM" ? "text-accent-orange" : "text-accent-green"}`}>
                                        {item.difference}
                                    </span>
                                </div>
                                <p className="text-[11px] text-text-secondary leading-relaxed bg-white/2 p-3 rounded-xs border border-border-color select-none">
                                    {item.description}
                                </p>
                            </div>
                        </Card>
                    </ScrollReveal>
                ))}
            </div>
        </section>
    );
}
