"use client";

import React, { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Search, Sparkles, GraduationCap, CornerDownLeft } from "lucide-react";
import { cn } from "@/utils/cn";

export function SpotlightSearch() {
    const [query, setQuery] = useState("");
    const [isFocused, setIsFocused] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setIsFocused(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if ((e.ctrlKey || e.metaKey) && e.key === "k") {
                e.preventDefault();
                setIsFocused(true);
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, []);

    const suggestions = [
        { label: "COEP Pune - Computer Engineering", type: "college", category: "Open Cutoff: 99.42%" },
        { label: "VJTI Mumbai - Information Technology", type: "college", category: "Open Cutoff: 99.28%" },
        { label: "Walchand Sangli - Electronics & Telecom", type: "college", category: "Open Cutoff: 97.80%" },
        { label: "What is my chance with 96.5 percentile?", type: "ai", category: "Ask AI Seeker" },
    ];

    const filteredSuggestions = query
        ? suggestions.filter((s) => s.label.toLowerCase().includes(query.toLowerCase()))
        : suggestions;

    return (
        <div ref={containerRef} className="max-w-2xl mx-auto w-full px-6 relative z-30">
            <div
                className={cn(
                    "glass-md rounded-md border border-border-color shadow-2xl transition-all duration-300 overflow-hidden",
                    isFocused ? "border-accent-cyan shadow-[0_0_30px_rgba(0,240,255,0.15)] scale-[1.01]" : "hover:border-white/15"
                )}
            >
                <div className="flex items-center gap-3.5 px-4 py-4">
                    <motion.div
                        animate={{ rotate: isFocused ? 90 : 0 }}
                        className="text-text-secondary shrink-0"
                    >
                        <Search className="w-5 h-5 text-accent-cyan" />
                    </motion.div>
                    <input
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        onFocus={() => setIsFocused(true)}
                        placeholder="Search colleges, branches, or ask AI... (Ctrl + K)"
                        className="w-full bg-transparent text-white placeholder-text-disabled text-sm outline-none border-none"
                    />
                    <div className="hidden sm:flex items-center gap-1 glass-sm px-2 py-1 rounded-xs border border-border-color select-none text-[10px] text-text-secondary shrink-0">
                        <span>Ctrl</span>
                        <span>+</span>
                        <span>K</span>
                    </div>
                </div>

                <AnimatePresence>
                    {isFocused && (
                        <motion.div
                            initial={{ height: 0, opacity: 0 }}
                            animate={{ height: "auto", opacity: 1 }}
                            exit={{ height: 0, opacity: 0 }}
                            className="border-t border-border-color/30 overflow-hidden"
                        >
                            <div className="p-2 flex flex-col">
                                {filteredSuggestions.length > 0 ? (
                                    filteredSuggestions.map((item, idx) => (
                                        <button
                                            key={idx}
                                            onClick={() => {
                                                setQuery(item.label);
                                                setIsFocused(false);
                                            }}
                                            className="w-full flex items-center justify-between p-3 rounded-xs text-left hover:bg-white/5 transition-all text-xs text-text-secondary hover:text-white cursor-pointer group"
                                        >
                                            <div className="flex items-center gap-3">
                                                {item.type === "ai" ? (
                                                    <Sparkles className="w-4 h-4 text-accent-purple shrink-0" />
                                                ) : (
                                                    <GraduationCap className="w-4 h-4 text-accent-cyan shrink-0" />
                                                )}
                                                <span>{item.label}</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <span className="text-[10px] text-text-tertiary select-none group-hover:text-white/60">
                                                    {item.category}
                                                </span>
                                                <CornerDownLeft className="w-3.5 h-3.5 text-text-tertiary opacity-0 group-hover:opacity-100 transition-opacity shrink-0" />
                                            </div>
                                        </button>
                                    ))
                                ) : (
                                    <div className="p-4 text-center text-xs text-text-tertiary">
                                        No matching results found.
                                    </div>
                                )}
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>
        </div>
    );
}
