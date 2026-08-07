"use client";

import React from "react";
import { motion } from "framer-motion";
import { cn } from "@/utils/cn";
import { Loader2, Check, AlertCircle } from "lucide-react";

interface ButtonProps extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, 'onAnimationStart' | 'onDragStart' | 'onDragEnd' | 'onDrag'> {
    variant?: "primary" | "secondary" | "accent" | "danger";
    size?: "sm" | "md" | "lg";
    isLoading?: boolean;
    isSuccess?: boolean;
    isError?: boolean;
}

export function Button({
    children,
    className,
    variant = "primary",
    size = "md",
    isLoading = false,
    isSuccess = false,
    isError = false,
    disabled,
    ...props
}: ButtonProps) {
    const baseStyle = "relative inline-flex items-center justify-center font-medium rounded-sm transition-all select-none overflow-hidden cursor-pointer";

    const sizeStyle = {
        sm: "px-3 py-1.5 text-xs gap-1.5",
        md: "px-5 py-2.5 text-sm gap-2",
        lg: "px-8 py-3.5 text-md gap-2.5",
    }[size];

    const variantStyle = {
        primary: "bg-white text-black hover:bg-neutral-100 shadow-[0_0_20px_rgba(255,255,255,0.06)] active:scale-95 disabled:bg-neutral-800 disabled:text-neutral-500",
        secondary: "glass-sm text-white hover:bg-white/10 border border-border-color hover:border-white/15 disabled:opacity-50",
        accent: "bg-accent-purple text-white hover:bg-opacity-90 shadow-[0_0_20px_rgba(138,43,226,0.2)] disabled:opacity-50",
        danger: "bg-accent-orange text-white hover:bg-opacity-90 shadow-[0_0_20px_rgba(255,94,0,0.2)] disabled:opacity-50",
    }[variant];

    return (
        <motion.button
            whileHover={{ scale: disabled ? 1 : 1.02 }}
            whileTap={{ scale: disabled ? 1 : 0.98 }}
            disabled={disabled || isLoading}
            className={cn(baseStyle, sizeStyle, variantStyle, className)}
            {...props}
        >
            {isLoading && <Loader2 className="w-4 h-4 animate-spin shrink-0" />}
            {isSuccess && <Check className="w-4 h-4 text-accent-green shrink-0" />}
            {isError && <AlertCircle className="w-4 h-4 text-accent-orange shrink-0" />}
            {!isLoading && !isSuccess && !isError && children}
        </motion.button>
    );
}
