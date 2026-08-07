"use client";

import React from "react";
import { motion } from "framer-motion";
import { LayoutDashboard, Compass, Sparkles, FolderHeart, Milestone, Settings } from "lucide-react";
import { cn } from "@/utils/cn";

export interface SidebarItem {
    id: string;
    label: string;
    icon: React.ReactNode;
}

interface SidebarProps {
    activeItem: string;
    onChange: (id: string) => void;
    className?: string;
}

export function Sidebar({ activeItem, onChange, className }: SidebarProps) {
    const items: SidebarItem[] = [
        { id: "dashboard", label: "Dashboard", icon: <LayoutDashboard className="w-4 h-4" /> },
        { id: "explore", label: "Explore Colleges", icon: <Compass className="w-4 h-4" /> },
        { id: "recommendations", label: "Smart Matches", icon: <Sparkles className="w-4 h-4" /> },
        { id: "wishlist", label: "My Wishlist", icon: <FolderHeart className="w-4 h-4" /> },
        { id: "admission", label: "Admission Journey", icon: <Milestone className="w-4 h-4" /> },
    ];

    return (
        <aside className={cn("hidden md:flex flex-col h-screen w-64 glass-sidebar fixed left-0 top-0 pt-24 pb-8 px-4 justify-between z-30", className)}>
            <div className="flex flex-col gap-1.5">
                {items.map((item) => {
                    const isActive = item.id === activeItem;
                    return (
                        <button
                            key={item.id}
                            onClick={() => onChange(item.id)}
                            className={cn(
                                "relative flex items-center gap-3 px-4 py-3 text-xs font-semibold select-none rounded-sm transition-colors cursor-pointer w-full text-left",
                                isActive ? "text-black" : "text-text-secondary hover:text-white"
                            )}
                        >
                            {isActive && (
                                <motion.div
                                    layoutId="active-sidebar-pill"
                                    className="absolute inset-0 bg-white rounded-xs z-0"
                                    transition={{ type: "spring", stiffness: 300, damping: 25 }}
                                />
                            )}
                            <span className="relative z-10 shrink-0">{item.icon}</span>
                            <span className="relative z-10">{item.label}</span>
                        </button>
                    );
                })}
            </div>

            <button className="flex items-center gap-3 px-4 py-3 text-xs font-semibold text-text-secondary hover:text-white transition-colors cursor-pointer w-full text-left rounded-sm select-none">
                <Settings className="w-4 h-4" />
                <span>Settings</span>
            </button>
        </aside>
    );
}
