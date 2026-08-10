"use client";

import React from "react";
import { Card } from "../ui/Card";
import { ScrollReveal } from "../animations/ScrollReveal";
import { Sparkles, Search, History, Grid, CheckSquare, Heart } from "lucide-react";

export function FeatureGrid() {
    const features = [
        {
            title: "Smart College Matching",
            description: "Intelligent match score calculated dynamically using score, category, seat matrix, and branch preferences.",
            icon: <Sparkles className="w-5 h-5 text-accent-purple" />,
            glow: "rgba(138, 43, 226, 0.15)",
        },
        {
            title: "College Discovery Search",
            description: "Filter colleges using NAAC grades, fees, package ratios, and NBA accreditation parameters.",
            icon: <Search className="w-5 h-5 text-accent-cyan" />,
            glow: "rgba(0, 240, 255, 0.15)",
        },
        {
            title: "Historical Cutoff Analytics",
            description: "Explore comprehensive admission cutoffs spanning multiple rounds and categories.",
            icon: <History className="w-5 h-5 text-accent-orange" />,
            glow: "rgba(255, 94, 0, 0.12)",
        },
        {
            title: "Seat Matrix Audit",
            description: "Verify intake capacity, vacancy numbers, and category matrix breakdowns.",
            icon: <Grid className="w-5 h-5 text-accent-green" />,
            glow: "rgba(57, 255, 20, 0.12)",
        },
        {
            title: "Admission Milestone Tracker",
            description: "Track document uploads, seat allocations, and confirmation timelines via state validation.",
            icon: <CheckSquare className="w-5 h-5 text-accent-cyan" />,
            glow: "rgba(0, 240, 255, 0.15)",
        },
        {
            title: "Optimistic Wishlists",
            description: "Save favorite colleges, order branch preferences, and write private notes securely.",
            icon: <Heart className="w-5 h-5 text-accent-purple" />,
            glow: "rgba(138, 43, 226, 0.15)",
        },
    ];

    return (
        <section id="features" className="relative py-24 bg-primary-bg max-w-5xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="text-center mb-16 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">Features</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        Designed for ambitious students.
                    </h2>
                    <p className="text-xs text-text-secondary max-w-lg leading-relaxed mt-1">
                        CampusSeekers consolidates data, matching engines, planning workflows, and exports into one premium, clean design.
                    </p>
                </div>
            </ScrollReveal>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {features.map((item, idx) => (
                    <ScrollReveal key={idx} delay={idx * 0.05}>
                        <Card className="flex flex-col gap-4 h-full" glowColor={item.glow}>
                            <div className="flex items-center justify-center w-10 h-10 rounded-sm bg-white/5 border border-border-color shrink-0">
                                {item.icon}
                            </div>
                            <div className="flex flex-col gap-1">
                                <h4 className="text-sm font-bold text-white select-none">{item.title}</h4>
                                <p className="text-xs text-text-secondary leading-relaxed select-none">{item.description}</p>
                            </div>
                        </Card>
                    </ScrollReveal>
                ))}
            </div>
        </section>
    );
}
