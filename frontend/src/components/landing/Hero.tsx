"use client";

import React, { useRef } from "react";
import { motion, useMotionTemplate, useMotionValue } from "framer-motion";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";
import { Magnetic } from "../animations/Magnetic";
import { Sparkles, ArrowRight, Play } from "lucide-react";

export function Hero() {
    const mouseX = useMotionValue(0);
    const mouseY = useMotionValue(0);
    const containerRef = useRef<HTMLDivElement>(null);

    const handleMouseMove = (e: React.MouseEvent) => {
        if (!containerRef.current) return;
        const { left, top } = containerRef.current.getBoundingClientRect();
        mouseX.set(e.clientX - left);
        mouseY.set(e.clientY - top);
    };

    const bgGlow = useMotionTemplate`radial-gradient(600px circle at ${mouseX}px ${mouseY}px, rgba(0, 240, 255, 0.08), rgba(138, 43, 226, 0.04) 50%, transparent 80%)`;

    return (
        <section
            ref={containerRef}
            onMouseMove={handleMouseMove}
            className="relative min-h-[90vh] flex flex-col items-center justify-center text-center px-6 overflow-hidden bg-primary-bg pt-20"
        >
            <motion.div
                className="pointer-events-none absolute inset-0 z-0"
                style={{ background: bgGlow }}
            />

            <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0">
                <div className="absolute top-[20%] left-[10%] w-72 h-72 bg-accent-purple rounded-full blur-[120px] animate-ambient-breathe" />
                <div className="absolute bottom-[20%] right-[10%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px] animate-ambient-breathe" />
            </div>

            <div className="relative z-10 flex flex-col items-center gap-6 max-w-4xl">
                <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.6, ease: "easeOut" }}
                >
                    <Badge variant="purple" glow className="px-3 py-1 select-none">
                        <Sparkles className="w-3.5 h-3.5 mr-1.5 inline text-accent-cyan animate-pulse" />
                        AI-Powered Admission Matching
                    </Badge>
                </motion.div>

                <motion.h1
                    initial={{ opacity: 0, letterSpacing: "0.1em" }}
                    animate={{ opacity: 1, letterSpacing: "0.25em" }}
                    transition={{ duration: 1.2, ease: [0.16, 1, 0.3, 1] }}
                    className="font-futuristic font-extralight text-4xl md:text-7xl text-white tracking-[0.25em] uppercase select-none mt-2 drop-shadow-[0_0_30px_rgba(255,255,255,0.1)]"
                >
                    CAMPUSSEEKERS
                </motion.h1>

                <motion.h2
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
                    className="text-2xl md:text-5xl font-extrabold text-white leading-tight select-none mt-2"
                >
                    Find Your Future.<br />
                    <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan via-accent-purple to-accent-orange">
                        Not Just Your College.
                    </span>
                </motion.h2>

                <motion.p
                    initial={{ opacity: 0, y: 15 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8, delay: 0.3, ease: [0.16, 1, 0.3, 1] }}
                    className="text-sm md:text-md text-text-secondary max-w-xl select-none leading-relaxed mt-2"
                >
                    AI-powered recommendations, historical cutoffs, intelligent search, and admission planning in one premium platform.
                </motion.p>

                <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8, delay: 0.4, ease: [0.16, 1, 0.3, 1] }}
                    className="flex flex-wrap items-center justify-center gap-4 mt-6"
                >
                    <Magnetic>
                        <Button variant="primary" size="lg">
                            Get Started <ArrowRight className="w-4 h-4 ml-1.5" />
                        </Button>
                    </Magnetic>
                    <Magnetic>
                        <Button variant="secondary" size="lg">
                            Explore Colleges
                        </Button>
                    </Magnetic>
                    <Magnetic>
                        <Button variant="secondary" size="lg" className="gap-2">
                            <Play className="w-4 h-4 text-accent-cyan fill-accent-cyan" />
                            Watch Demo
                        </Button>
                    </Magnetic>
                </motion.div>
            </div>
        </section>
    );
}
