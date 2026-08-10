"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Card } from "../ui/Card";
import { Badge } from "../ui/Badge";
import { Tabs } from "../ui/Tabs";
import { ScrollReveal } from "../animations/ScrollReveal";
import {
    LayoutDashboard,
    Search,
    Sparkles,
    FolderHeart,
    Milestone,
    Lock
} from "lucide-react";

export function PlatformPreview() {
    const [activeTab, setActiveTab] = useState("dashboard");

    const tabs = [
        { id: "dashboard", label: "Dashboard", icon: <LayoutDashboard className="w-3.5 h-3.5" /> },
        { id: "search", label: "Colleges", icon: <Search className="w-3.5 h-3.5" /> },
        { id: "recommendations", label: "Matches", icon: <Sparkles className="w-3.5 h-3.5" /> },
        { id: "wishlist", label: "Wishlist", icon: <FolderHeart className="w-3.5 h-3.5" /> },
        { id: "tracker", label: "Tracker", icon: <Milestone className="w-3.5 h-3.5" /> },
    ];

    return (
        <section className="relative py-24 bg-primary-bg max-w-5xl mx-auto w-full px-6 z-10">
            <ScrollReveal>
                <div className="text-center mb-12 flex flex-col items-center gap-2">
                    <span className="text-[10px] text-accent-cyan font-bold uppercase tracking-wider">Interface</span>
                    <h2 className="text-2xl md:text-4xl font-extrabold text-white mt-1">
                        Intuitive Application Console
                    </h2>
                    <p className="text-xs text-text-secondary max-w-lg leading-relaxed mt-1">
                        Take a look at the consolidated dashboard workflow showing statistics, target recommendations, and timelines.
                    </p>
                </div>
            </ScrollReveal>

            <ScrollReveal delay={0.15}>
                <div className="glass-md rounded-md border border-border-color shadow-2xl overflow-hidden max-w-4xl mx-auto">
                    <div className="flex items-center justify-between px-6 py-4 border-b border-border-color/30 bg-white/2 select-none">
                        <div className="flex gap-2">
                            <div className="w-3 h-3 rounded-full bg-accent-orange/40" />
                            <div className="w-3 h-3 rounded-full bg-accent-cyan/25" />
                            <div className="w-3 h-3 rounded-full bg-accent-green/25" />
                        </div>
                        <div className="flex items-center gap-1.5 glass-sm px-3 py-1 rounded-sm border border-border-color text-[10px] text-text-secondary">
                            <Lock className="w-3 h-3 text-text-tertiary" />
                            <span>app.campusseekers.com</span>
                        </div>
                        <div className="w-14" />
                    </div>

                    <div className="flex justify-center py-4 border-b border-border-color/20 bg-primary-bg/50">
                        <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />
                    </div>

                    <div className="p-6 md:p-8 min-h-[360px] bg-[#08080A]/60 relative overflow-hidden">
                        <AnimatePresence mode="wait">
                            {activeTab === "dashboard" && (
                                <motion.div
                                    key="dashboard"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.2 }}
                                    className="grid grid-cols-1 md:grid-cols-3 gap-6"
                                >
                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false}>
                                        <span className="text-[10px] font-semibold text-text-tertiary uppercase tracking-wider">Wishlist Summary</span>
                                        <span className="text-2xl font-light font-futuristic text-white">05</span>
                                        <span className="text-[10px] text-text-secondary">Favorited colleges saved</span>
                                    </Card>
                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false}>
                                        <span className="text-[10px] font-semibold text-text-tertiary uppercase tracking-wider">Shortlisted Branches</span>
                                        <span className="text-2xl font-light font-futuristic text-white">12</span>
                                        <span className="text-[10px] text-text-secondary">High match score profiles</span>
                                    </Card>
                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false}>
                                        <span className="text-[10px] font-semibold text-text-tertiary uppercase tracking-wider">Active Trackers</span>
                                        <span className="text-2xl font-light font-futuristic text-white">03</span>
                                        <span className="text-[10px] text-text-secondary">Applications under process</span>
                                    </Card>
                                </motion.div>
                            )}

                            {activeTab === "search" && (
                                <motion.div
                                    key="search"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.2 }}
                                    className="flex flex-col gap-4"
                                >
                                    <div className="flex gap-4">
                                        <div className="flex-1 glass-sm px-4 py-2 text-xs border border-border-color rounded-sm text-text-tertiary select-none">
                                            COEP Technological University
                                        </div>
                                        <div className="glass-sm px-4 py-2 text-xs border border-border-color rounded-sm text-text-tertiary select-none">
                                            Open Category
                                        </div>
                                    </div>
                                    <div className="border border-border-color/30 rounded-md overflow-hidden bg-white/2 p-4 text-xs flex flex-col gap-2">
                                        <div className="flex justify-between font-bold text-white border-b border-border-color/20 pb-2">
                                            <span>College Name</span>
                                            <span>Code</span>
                                            <span>Fees/Yr</span>
                                        </div>
                                        <div className="flex justify-between text-text-secondary pt-1">
                                            <span>COEP Technological University</span>
                                            <span>1002</span>
                                            <span>1.25 L</span>
                                        </div>
                                        <div className="flex justify-between text-text-secondary">
                                            <span>VJTI Mumbai</span>
                                            <span>3012</span>
                                            <span>1.15 L</span>
                                        </div>
                                    </div>
                                </motion.div>
                            )}

                            {activeTab === "recommendations" && (
                                <motion.div
                                    key="recommendations"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.2 }}
                                    className="flex flex-col gap-4"
                                >
                                    <div className="glass-md p-4 border border-border-color rounded-md flex justify-between items-center">
                                        <div className="flex flex-col gap-1">
                                            <Badge variant="cyan">TARGET Match</Badge>
                                            <span className="text-xs font-bold text-white">Walchand College of Engineering</span>
                                            <span className="text-[10px] text-text-tertiary">Computer Science</span>
                                        </div>
                                        <span className="text-xs text-accent-green font-semibold">+0.42%</span>
                                    </div>
                                </motion.div>
                            )}

                            {activeTab === "wishlist" && (
                                <motion.div
                                    key="wishlist"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.2 }}
                                    className="flex flex-col gap-3"
                                >
                                    <div className="glass-sm p-4 border border-border-color rounded-sm flex justify-between items-center text-xs">
                                        <span className="text-white font-semibold">COEP Technological University</span>
                                        <span className="text-text-tertiary">Pune, MH</span>
                                    </div>
                                    <div className="glass-sm p-4 border border-border-color rounded-sm flex justify-between items-center text-xs">
                                        <span className="text-white font-semibold">Sardar Patel College of Engineering</span>
                                        <span className="text-text-tertiary">Mumbai, MH</span>
                                    </div>
                                </motion.div>
                            )}

                            {activeTab === "tracker" && (
                                <motion.div
                                    key="tracker"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.2 }}
                                    className="flex flex-col gap-4"
                                >
                                    <div className="flex flex-col gap-2">
                                        <div className="flex justify-between items-center text-xs">
                                            <span className="text-white font-semibold">VJTI Mumbai - IT</span>
                                            <Badge variant="cyan" glow>APPLIED</Badge>
                                        </div>
                                        <div className="w-full bg-white/5 border border-border-color/30 rounded-full h-1.5 overflow-hidden">
                                            <div className="h-full bg-accent-cyan w-1/3" />
                                        </div>
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </div>
            </ScrollReveal>
        </section>
    );
}
