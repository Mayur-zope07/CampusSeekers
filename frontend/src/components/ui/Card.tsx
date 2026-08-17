"use client";

import React, { useRef } from "react";
import { motion, useMotionTemplate, useMotionValue } from "framer-motion";
import { cn } from "@/utils/cn";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
    children: React.ReactNode;
    glowColor?: string;
    hoverLift?: boolean;
    allowOverflow?: boolean;
}

export function Card({
    children,
    className,
    glowColor = "rgba(138, 43, 226, 0.15)",
    hoverLift = true,
    allowOverflow = false,
    ...props
}: CardProps) {
    const mouseX = useMotionValue(0);
    const mouseY = useMotionValue(0);
    const ref = useRef<HTMLDivElement>(null);

    const handleMouseMove = (e: React.MouseEvent) => {
        if (!ref.current) return;
        const { left, top } = ref.current.getBoundingClientRect();
        mouseX.set(e.clientX - left);
        mouseY.set(e.clientY - top);
    };

    const bgGlow = useMotionTemplate`radial-gradient(350px circle at ${mouseX}px ${mouseY}px, ${glowColor}, transparent 80%)`;

    return (
        <div
            ref={ref}
            onMouseMove={handleMouseMove}
            className={cn(
                "relative glass-md rounded-md p-6 border border-border-color shadow-lg transition-all duration-300 group",
                !allowOverflow && "overflow-hidden",
                hoverLift && "hover:-translate-y-1 hover:shadow-2xl hover:border-white/15",
                className
            )}
            {...props}
        >
            <motion.div
                className="pointer-events-none absolute -inset-px rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-500"
                style={{ background: bgGlow }}
            />
            <div className="relative z-10">{children}</div>
        </div>
    );
}
