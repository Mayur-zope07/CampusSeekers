import React from "react";
import { cn } from "@/utils/cn";

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
    variant?: "default" | "cyan" | "purple" | "green" | "orange";
    glow?: boolean;
}

export function Badge({
    children,
    className,
    variant = "default",
    glow = false,
    ...props
}: BadgeProps) {
    const baseStyle = "inline-flex items-center px-2 py-0.5 text-[10px] font-bold tracking-wider uppercase rounded-xs border select-none transition-all";

    const variantStyle = {
        default: "bg-white/5 text-text-secondary border-border-color",
        cyan: "bg-accent-cyan/10 text-accent-cyan border-accent-cyan/20",
        purple: "bg-accent-purple/10 text-accent-purple border-accent-purple/20",
        green: "bg-accent-green/10 text-accent-green border-accent-green/20",
        orange: "bg-accent-orange/10 text-accent-orange border-accent-orange/20",
    }[variant];

    const glowStyle = glow && variant !== "default"
        ? {
            cyan: "shadow-[0_0_10px_rgba(0,240,255,0.2)]",
            purple: "shadow-[0_0_10px_rgba(138,43,226,0.2)]",
            green: "shadow-[0_0_10px_rgba(57,255,20,0.2)]",
            orange: "shadow-[0_0_10px_rgba(255,94,0,0.2)]",
        }[variant as "cyan" | "purple" | "green" | "orange"]
        : "";

    return (
        <span className={cn(baseStyle, variantStyle, glowStyle, className)} {...props}>
            {children}
        </span>
    );
}
