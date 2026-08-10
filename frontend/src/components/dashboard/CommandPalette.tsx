"use client";

import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";
import { motion, AnimatePresence } from "framer-motion";
import { Search, Sparkles, Heart, LayoutDashboard, User, LogOut } from "lucide-react";

interface CommandPaletteProps {
    isOpen: boolean;
    onClose: () => void;
}

export function CommandPalette({ isOpen, onClose }: CommandPaletteProps) {
    const router = useRouter();
    const { logout } = useAuth();
    const [query, setQuery] = useState("");
    const containerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                onClose();
            }
        };
        if (isOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [isOpen, onClose]);

    const commands = [
        { label: "Dashboard Hub", icon: <LayoutDashboard className="w-4 h-4 text-accent-cyan" />, action: () => router.push("/app/dashboard") },
        { label: "Search Colleges", icon: <Search className="w-4 h-4 text-accent-cyan" />, action: () => router.push("/showcase") },
        { label: "Generate Recommendation", icon: <Sparkles className="w-4 h-4 text-accent-purple" />, action: () => router.push("/app/onboarding") },
        { label: "View Wishlist", icon: <Heart className="w-4 h-4 text-accent-orange" />, action: () => router.push("/app/dashboard") },
        { label: "Profile Settings", icon: <User className="w-4 h-4 text-text-secondary" />, action: () => router.push("/app/onboarding") },
        { label: "System Log Out", icon: <LogOut className="w-4 h-4 text-accent-orange" />, action: () => logout() },
    ];

    const filtered = query
        ? commands.filter((c) => c.label.toLowerCase().includes(query.toLowerCase()))
        : commands;

    return (
        <AnimatePresence>
            {isOpen && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-start justify-center pt-24 px-6 z-50">
                    <motion.div
                        initial={{ opacity: 0, scale: 0.95, y: -20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95, y: -20 }}
                        ref={containerRef}
                        className="w-full max-w-lg glass-dialog border border-border-color rounded-md overflow-hidden shadow-2xl"
                    >
                        <div className="flex items-center gap-3.5 px-4 py-4 border-b border-border-color/30">
                            <Search className="w-5 h-5 text-accent-cyan shrink-0 animate-pulse" />
                            <input
                                type="text"
                                placeholder="Type a workspace command..."
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                className="w-full bg-transparent text-white placeholder-text-disabled text-sm outline-none border-none"
                                autoFocus
                            />
                            <button onClick={onClose} className="text-xs text-text-tertiary hover:text-white transition-colors cursor-pointer select-none">
                                ESC
                            </button>
                        </div>
                        <div className="p-2 flex flex-col gap-1 max-h-72 overflow-y-auto">
                            {filtered.length > 0 ? (
                                filtered.map((cmd, idx) => (
                                    <button
                                        key={idx}
                                        onClick={() => {
                                            cmd.action();
                                            onClose();
                                        }}
                                        className="w-full flex items-center gap-3.5 p-3 rounded-xs text-left hover:bg-white/5 transition-all text-xs text-text-secondary hover:text-white cursor-pointer group"
                                    >
                                        {cmd.icon}
                                        <span className="flex-1 font-semibold">{cmd.label}</span>
                                    </button>
                                ))
                            ) : (
                                <div className="p-4 text-center text-xs text-text-tertiary select-none">
                                    No commands match your query.
                                </div>
                            )}
                        </div>
                    </motion.div>
                </div>
            )}
        </AnimatePresence>
    );
}
