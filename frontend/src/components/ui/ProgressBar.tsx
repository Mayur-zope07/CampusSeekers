"use client";

import React from "react";
import { motion } from "framer-motion";
import { cn } from "@/utils/cn";

interface ProgressBarProps {
    value: number; // 0 to 100
    glow?: boolean;
    className?: string;
}

export function ProgressBar({ value, glow = true, className }: ProgressBarProps) {
    const clampedValue = Math.min(Math.max(value, 0), 100);

    return (
        <div className={cn("w-full bg-white/5 border border-border-color/30 rounded-full h-2 relative overflow-hidden", className)}>
            <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${clampedValue}%` }}
                transition={{ type: "spring", stiffness: 80, damping: 15 }}
                className={cn(
                    "h-full rounded-full bg-gradient-to-r from-accent-purple to-accent-cyan",
                    glow && "shadow-[0_0_10px_rgba(0,240,255,0.4)]"
                )}
            />
        </div>
    );
}
