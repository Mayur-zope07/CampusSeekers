"use client";

import React from "react";
import { motion } from "framer-motion";
import { cn } from "@/utils/cn";

export interface TabItem {
    id: string;
    label: string;
    icon?: React.ReactNode;
}

interface TabsProps {
    tabs: TabItem[];
    activeTab: string;
    onChange: (id: string) => void;
    className?: string;
}

export function Tabs({ tabs, activeTab, onChange, className }: TabsProps) {
    return (
        <div className={cn("flex glass-sm p-1 rounded-sm border border-border-color w-fit gap-1", className)}>
            {tabs.map((tab) => {
                const isActive = tab.id === activeTab;
                return (
                    <button
                        key={tab.id}
                        onClick={() => onChange(tab.id)}
                        className={cn(
                            "relative flex items-center gap-2 px-4 py-2 text-xs font-semibold select-none transition-colors duration-300 rounded-sm cursor-pointer",
                            isActive ? "text-black" : "text-text-secondary hover:text-white"
                        )}
                    >
                        {isActive && (
                            <motion.div
                                layoutId="active-tab-indicator"
                                className="absolute inset-0 bg-white rounded-xs z-0"
                                transition={{ type: "spring", stiffness: 350, damping: 28 }}
                            />
                        )}
                        <span className="relative z-10 flex items-center gap-1.5">
                            {tab.icon && <span className="shrink-0">{tab.icon}</span>}
                            {tab.label}
                        </span>
                    </button>
                );
            })}
        </div>
    );
}
