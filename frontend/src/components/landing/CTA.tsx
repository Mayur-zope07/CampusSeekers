"use client";

import React from "react";
import { Card } from "../ui/Card";
import { Button } from "../ui/Button";
import { Magnetic } from "../animations/Magnetic";
import { ScrollReveal } from "../animations/ScrollReveal";
import { ArrowUpRight } from "lucide-react";

export function CTA() {
    return (
        <section className="relative py-24 bg-primary-bg max-w-5xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <Card
                    className="flex flex-col items-center justify-center text-center p-12 overflow-hidden relative"
                    glowColor="rgba(0, 240, 255, 0.15)"
                    hoverLift={false}
                >
                    <div className="absolute inset-0 opacity-[0.02] pointer-events-none">
                        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-accent-purple rounded-full blur-[100px]" />
                    </div>

                    <h2 className="text-2xl md:text-4xl font-extrabold text-white leading-tight mb-4 select-none relative z-10">
                        Start Your Journey Today.
                    </h2>
                    <p className="text-xs text-text-secondary max-w-md mb-8 select-none relative z-10">
                        Discover historical trends, evaluate admission options, order shortlists, and monitor active applications.
                    </p>

                    <div className="flex flex-wrap gap-4 relative z-10">
                        <Magnetic>
                            <Button variant="primary" size="lg">
                                Create Account <ArrowUpRight className="w-4 h-4 ml-1 shrink-0" />
                            </Button>
                        </Magnetic>
                        <Magnetic>
                            <Button variant="secondary" size="lg">
                                Explore Colleges
                            </Button>
                        </Magnetic>
                    </div>
                </Card>
            </ScrollReveal>
        </section>
    );
}
