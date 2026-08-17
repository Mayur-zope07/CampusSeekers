"use client";

import React, { useEffect, useState, useRef } from "react";
import { useInView } from "framer-motion";
import { Card } from "../ui/Card";
import { ScrollReveal } from "../animations/ScrollReveal";

interface CounterProps {
    value: number;
    suffix?: string;
    duration?: number;
}

function Counter({ value, suffix = "", duration = 1500 }: CounterProps) {
    const [count, setCount] = useState(0);
    const ref = useRef<HTMLSpanElement>(null);
    const isInView = useInView(ref, { once: true });

    useEffect(() => {
        if (!isInView) return;

        const start = 0;
        const end = value;
        const totalSteps = 60;
        const stepTime = duration / totalSteps;
        let currentStep = 0;

        const timer = setInterval(() => {
            currentStep++;
            const progress = currentStep / totalSteps;
            const easeProgress = progress * (2 - progress);
            const currentCount = Math.floor(easeProgress * (end - start) + start);

            setCount(currentCount);

            if (currentStep >= totalSteps) {
                setCount(end);
                clearInterval(timer);
            }
        }, stepTime);

        return () => clearInterval(timer);
    }, [isInView, value, duration]);

    return (
        <span ref={ref} className="font-futuristic font-light text-xl md:text-2xl lg:text-3xl xl:text-4xl text-white tracking-wider">
            {count.toLocaleString()}{suffix}
        </span>
    );
}

export function Statistics() {
    const stats = [
        { label: "Colleges Indexed", value: 372, suffix: "+" },
        { label: "Academic Branches", value: 2158, suffix: "" },
        { label: "Historical Cutoffs", value: 13745, suffix: "" },
        { label: "Seat Matrix Records", value: 45000, suffix: "+" },
        { label: "Matching Accuracy", value: 98, suffix: ".6%" },
    ];

    return (
        <section id="stats" className="relative py-16 bg-primary-bg max-w-5xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                    {stats.map((item, idx) => (
                        <Card key={idx} className="flex flex-col items-center justify-center p-4 text-center" glowColor="rgba(0, 240, 255, 0.08)" hoverLift={false}>
                            <Counter value={item.value} suffix={item.suffix} />
                            <span className="text-[8px] sm:text-[9px] md:text-[10px] text-text-secondary font-semibold uppercase tracking-wider mt-2">
                                {item.label}
                            </span>
                        </Card>
                    ))}
                </div>
            </ScrollReveal>
        </section>
    );
}
