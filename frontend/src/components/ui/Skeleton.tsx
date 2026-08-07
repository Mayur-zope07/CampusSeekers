import React from "react";
import { cn } from "@/utils/cn";

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
    variant?: "text" | "rectangular" | "circle";
}

export function Skeleton({ className, variant = "rectangular", ...props }: SkeletonProps) {
    return (
        <div
            className={cn(
                "bg-white/5 animate-pulse border border-border-color/30",
                variant === "rectangular" && "rounded-sm",
                variant === "text" && "h-4 w-3/4 rounded-xs",
                variant === "circle" && "rounded-full",
                className
            )}
            {...props}
        />
    );
}
