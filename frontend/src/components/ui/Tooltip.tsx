"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/utils/cn";

interface TooltipProps {
    content: string;
    children: React.ReactNode;
    placement?: "top" | "bottom" | "left" | "right";
    className?: string;
}

export function Tooltip({ children, content, placement = "top", className }: TooltipProps) {
    const [isHovered, setIsHovered] = useState(false);

    const placementStyle = {
        top: "bottom-full left-1/2 -translate-x-1/2 mb-2",
        bottom: "top-full left-1/2 -translate-x-1/2 mt-2",
        left: "right-full top-1/2 -translate-y-1/2 mr-2",
        right: "left-full top-1/2 -translate-y-1/2 ml-2",
    }[placement];

    return (
        <div
            className="relative inline-block w-fit"
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            {children}
            <AnimatePresence>
                {isHovered && (
                    <motion.div
                        initial={{ opacity: 0, scale: 0.95 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        transition={{ duration: 0.12 }}
                        className={cn(
                            "absolute z-50 glass-md border border-border-color rounded-xs px-2.5 py-1.5 text-[10px] font-semibold text-white tracking-wide whitespace-nowrap shadow-lg select-none",
                            placementStyle,
                            className
                        )}
                    >
                        {content}
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}
