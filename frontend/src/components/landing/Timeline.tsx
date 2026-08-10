"use client";

import React from "react";
import { ScrollReveal } from "../animations/ScrollReveal";
import { Badge } from "../ui/Badge";
import { Search, Scale, BarChart2, Sparkles, Star, Milestone } from "lucide-react";

export function Timeline() {
    const steps = [
        {
            title: "Query & Search",
            description: "Search dynamically across hundreds of regional state institutions using flexible criteria metrics.",
            icon: <Search className="w-4 h-4 text-accent-cyan" />,
        },
        {
            title: "Compare Profiles",
            description: "Analyze academic packages, fee variables, NAAC accreditations, and placements in side-by-side matrices.",
            icon: <Scale className="w-4 h-4 text-accent-orange" />,
        },
        {
            title: "Analyze Cutoffs",
            description: "Review comprehensive cutoff datasets spanning previous years, rounds, categories, and branch tracks.",
            icon: <BarChart2 className="w-4 h-4 text-accent-green" />,
        },
        {
            title: "Get Smart Matches",
            description: "Receive list categorizations based on your target exam scores (SAFE, TARGET, DREAM matches).",
            icon: <Sparkles className="w-4 h-4 text-accent-purple" />,
        },
        {
            title: "Shortlist Branches",
            description: "Order preferences, assign priority ranking weights, and preserve custom planning notes.",
            icon: <Star className="w-4 h-4 text-accent-cyan" />,
        },
        {
            title: "Track Admission",
            description: "Validate seat allocation milestones chronologically using status progression charts.",
            icon: <Milestone className="w-4 h-4 text-accent-green" />,
        },
    ];

    return (
        <section id="how-it-works" className="relative py-24 bg-primary-bg max-w-4xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="text-center mb-20 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">Workflow</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        How CampusSeekers Works
                    </h2>
                    <p className="text-xs text-text-secondary max-w-lg leading-relaxed mt-1">
                        A structured six-step trajectory driving optimized matching, decision support, and application planning.
                    </p>
                </div>
            </ScrollReveal>

            <div className="relative border-l border-border-color/40 ml-4 md:ml-32 flex flex-col gap-12">
                {steps.map((step, idx) => (
                    <ScrollReveal key={idx} delay={idx * 0.05}>
                        <div className="relative pl-8 md:pl-12 group">
                            <div className="absolute -left-[17px] top-1.5 flex items-center justify-center w-8 h-8 rounded-full bg-primary-bg border border-border-color group-hover:border-accent-cyan group-hover:shadow-[0_0_10px_rgba(0,240,255,0.2)] transition-all duration-300 shrink-0">
                                {step.icon}
                            </div>

                            <div className="flex flex-col gap-1">
                                <div className="flex items-center gap-3">
                                    <Badge variant="default" className="text-[9px] py-0">Step {idx + 1}</Badge>
                                    <h4 className="text-sm font-bold text-white group-hover:text-accent-cyan transition-colors select-none">
                                        {step.title}
                                    </h4>
                                </div>
                                <p className="text-xs text-text-secondary leading-relaxed max-w-xl select-none mt-1">
                                    {step.description}
                                </p>
                            </div>
                        </div>
                    </ScrollReveal>
                ))}
            </div>
        </section>
    );
}
