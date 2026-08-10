"use client";

import React from "react";
import { Card } from "../ui/Card";
import { ScrollReveal } from "../animations/ScrollReveal";

export function Testimonials() {
    const items = [
        {
            quote: "CampusSeekers was the differentiator in my engineering search. The accuracy matching open cutoffs to my target score was flawless.",
            author: "Aman Deshmukh",
            details: "98.42 Percentile • COEP IT",
        },
        {
            quote: "The interface feels extremely premium, exactly like using macOS. Tracking admission progress step-by-step removed all anxiety.",
            author: "Pooja Patil",
            details: "97.80 Percentile • VJTI Electronics",
        },
        {
            quote: "Having placements packages and NAAC accreditation tables dynamically mapped in search fields saved me weeks of manual parsing.",
            author: "Rohan Sawant",
            details: "96.50 Percentile • SPIT Computer Science",
        },
        {
            quote: "I was recommended a Dream institution that was slightly higher than my target score and successfully cleared it in Round 2!",
            author: "Snehal Gore",
            details: "99.10 Percentile • PICT Pune",
        },
    ];

    return (
        <section className="relative py-24 bg-primary-bg overflow-hidden z-10">
            <ScrollReveal>
                <div className="text-center mb-16 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">Testimonials</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        Loved by high-performing students.
                    </h2>
                </div>
            </ScrollReveal>

            <div className="relative flex w-full overflow-x-hidden">
                <div className="flex gap-6 animate-[marquee_25s_linear_infinite] hover:[animation-play-state:paused] shrink-0">
                    {[...items, ...items].map((item, idx) => (
                        <Card
                            key={idx}
                            className="w-[300px] md:w-[360px] flex flex-col justify-between p-6 select-none"
                            glowColor="rgba(0, 240, 255, 0.05)"
                            hoverLift={false}
                        >
                            <p className="text-xs text-text-secondary leading-relaxed italic">
                                &ldquo;{item.quote}&rdquo;
                            </p>
                            <div className="flex flex-col gap-0.5 border-t border-border-color/30 pt-4 mt-4 shrink-0">
                                <span className="text-xs font-bold text-white">{item.author}</span>
                                <span className="text-[10px] text-text-tertiary">{item.details}</span>
                            </div>
                        </Card>
                    ))}
                </div>
            </div>

            <style jsx global>{`
                @keyframes marquee {
                    0% { transform: translateX(0%); }
                    100% { transform: translateX(-50%); }
                }
            `}</style>
        </section>
    );
}
