"use client";

import React, { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";
import { motion, useMotionTemplate, useMotionValue } from "framer-motion";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";
import { Dialog } from "../ui/Dialog";
import { Magnetic } from "../animations/Magnetic";
import { Sparkles, ArrowRight, Play, CheckCircle2, Star } from "lucide-react";

export function Hero() {
    const router = useRouter();
    const { isAuthenticated, checkProfileExistence } = useAuth();
    const [isRedirecting, setIsRedirecting] = useState(false);
    const [isDemoOpen, setIsDemoOpen] = useState(false);

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

    const handleGetStarted = async () => {
        if (isAuthenticated) {
            setIsRedirecting(true);
            try {
                const profileExists = await checkProfileExistence();
                if (profileExists) {
                    router.push("/app/dashboard");
                } else {
                    router.push("/app/onboarding");
                }
            } catch {
                router.push("/app/register");
            } finally {
                setIsRedirecting(false);
            }
        } else {
            router.push("/app/register");
        }
    };

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
                        <Button variant="primary" size="lg" onClick={handleGetStarted} isLoading={isRedirecting}>
                            Get Started <ArrowRight className="w-4 h-4 ml-1.5" />
                        </Button>
                    </Magnetic>
                    <Magnetic>
                        <Button variant="secondary" size="lg" onClick={() => router.push("/app/search")}>
                            Explore Colleges
                        </Button>
                    </Magnetic>
                    <Magnetic>
                        <Button variant="secondary" size="lg" className="gap-2" onClick={() => setIsDemoOpen(true)}>
                            <Play className="w-4 h-4 text-accent-cyan fill-accent-cyan" />
                            Watch Demo
                        </Button>
                    </Magnetic>
                </motion.div>
            </div>

            <Dialog isOpen={isDemoOpen} onClose={() => setIsDemoOpen(false)} title="CampusSeekers Platform Tour" className="max-w-2xl">
                <div className="flex flex-col gap-5 mt-2 text-left select-none">
                    <p className="text-xs text-text-secondary leading-relaxed">
                        CampusSeekers is an enterprise-grade smart matching platform designed to help students analyze cutoff probabilities, optimize wishlists, and track admission milestones in real-time.
                    </p>

                    <div className="border border-border-color/30 rounded-md p-4 bg-white/2 flex flex-col gap-4">
                        <div className="flex items-center justify-between border-b border-border-color/20 pb-2">
                            <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">Simulated Match Screen</span>
                            <Badge variant="cyan" className="py-0 px-2 text-[9px]">VJTI Mumbai Preview</Badge>
                        </div>

                        <div className="flex justify-between items-start gap-4">
                            <div className="flex flex-col gap-1 min-w-0">
                                <h4 className="font-bold text-sm text-white">VJTI Mumbai</h4>
                                <span className="text-xs text-text-tertiary">Information Technology • Government Autonomous</span>
                            </div>
                            <div className="flex flex-col items-end gap-1 shrink-0">
                                <Badge variant="cyan" glow>TARGET Match</Badge>
                                <span className="text-[10px] text-text-secondary">Chance: 78%</span>
                            </div>
                        </div>

                        <div className="grid grid-cols-3 gap-2 text-[10px] border-t border-border-color/20 pt-3 text-text-secondary">
                            <div>
                                <span className="text-text-tertiary block">Your Percentile</span>
                                <span className="font-bold text-white">99.12%</span>
                            </div>
                            <div>
                                <span className="text-text-tertiary block">Target Cutoff</span>
                                <span className="font-bold text-white">98.90%</span>
                            </div>
                            <div>
                                <span className="text-text-tertiary block">Margin Difference</span>
                                <span className="font-bold text-accent-green">+0.22%</span>
                            </div>
                        </div>

                        <div className="flex flex-col gap-1 text-[10px] bg-white/2 p-2.5 rounded-sm border border-border-color/20">
                            <div className="flex justify-between">
                                <span className="text-text-tertiary">Admission Tracker Status</span>
                                <span className="text-accent-cyan font-semibold">Documents Verified ✓</span>
                            </div>
                            <div className="h-1.5 w-full bg-border-color/20 rounded-full overflow-hidden mt-1">
                                <div className="h-full bg-gradient-to-r from-accent-cyan to-accent-green rounded-full" style={{ width: "66%" }} />
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 text-xs">
                        <div className="flex gap-2 items-start">
                            <CheckCircle2 className="w-4 h-4 text-accent-green shrink-0 mt-0.5" />
                            <div>
                                <span className="font-bold text-white block">Safe / Target Prediction</span>
                                <span className="text-[11px] text-text-secondary">Cutoff delta calculator classifies safe vs dream colleges.</span>
                            </div>
                        </div>
                        <div className="flex gap-2 items-start">
                            <Star className="w-4 h-4 text-accent-purple shrink-0 mt-0.5" />
                            <div>
                                <span className="font-bold text-white block">Priority Shortlists</span>
                                <span className="text-[11px] text-text-secondary">Drag, re-order, and assign customized weights.</span>
                            </div>
                        </div>
                    </div>

                    <div className="flex justify-end gap-2 border-t border-border-color/20 pt-4 mt-2">
                        <Button variant="secondary" onClick={() => setIsDemoOpen(false)}>
                            Close Demo
                        </Button>
                        <Button variant="primary" onClick={() => { setIsDemoOpen(false); handleGetStarted(); }}>
                            Get Started Now
                        </Button>
                    </div>
                </div>
            </Dialog>
        </section>
    );
}
