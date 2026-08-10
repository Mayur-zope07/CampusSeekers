"use client";

import React from "react";
import { Accordion } from "../ui/Accordion";
import { ScrollReveal } from "../animations/ScrollReveal";

export function FAQ() {
    const faqItems = [
        {
            id: "q1",
            title: "Is the recommendation engine utilizing ML or AI?",
            content: "No. CampusSeekers matching is computed entirely using historical cutoff percentiles, category seat allocations, and configurable parameters. This guarantees consistent, reproducible, and verifiable results.",
        },
        {
            id: "q2",
            title: "How are SAFE, TARGET, and DREAM categories determined?",
            content: "Matches are categorized based on the safety margin difference between your score and the institutions historical cutoff. SAFE matches are significantly below your score, TARGET matches are within a small margin, and DREAM matches are slightly above your score.",
        },
        {
            id: "q3",
            title: "Can I export my dashboard and trackers?",
            content: "Yes. CampusSeekers supports exporting your student dashboard overview, recommendations, and active trackers as structured CSV files or formatted PDF reports at any time.",
        },
        {
            id: "q4",
            title: "Are category seat allocations supported in searches?",
            content: "Yes. All search, comparisons, and cutoff analytics resolve candidate matches relative to specific state category filters (Open, OBC, SC, ST, EWS, etc.).",
        },
    ];

    return (
        <section id="faq" className="relative py-24 bg-primary-bg max-w-3xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="text-center mb-16 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">FAQ</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        Frequently Asked Questions
                    </h2>
                </div>
            </ScrollReveal>

            <ScrollReveal delay={0.15}>
                <Accordion items={faqItems} />
            </ScrollReveal>
        </section>
    );
}
