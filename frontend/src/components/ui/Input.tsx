"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/utils/cn";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    success?: boolean;
    icon?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
    ({ className, label, error, success, icon, type = "text", id, ...props }, ref) => {
        const [isFocused, setIsFocused] = useState(false);
        const inputId = id || Math.random().toString(36).substring(2, 9);

        return (
            <div className="flex flex-col gap-1.5 w-full relative">
                {label && (
                    <label
                        htmlFor={inputId}
                        className={cn(
                            "text-xs font-semibold select-none transition-colors duration-200",
                            isFocused ? "text-accent-cyan" : "text-text-secondary",
                            error && "text-accent-orange"
                        )}
                    >
                        {label}
                    </label>
                )}
                <div className="relative flex items-center">
                    {icon && (
                        <div className="absolute left-3.5 text-text-tertiary pointer-events-none shrink-0">
                            {icon}
                        </div>
                    )}
                    <input
                        ref={ref}
                        id={inputId}
                        type={type}
                        onFocus={() => setIsFocused(true)}
                        onBlur={() => setIsFocused(false)}
                        className={cn(
                            "w-full glass-sm rounded-sm text-sm text-white placeholder-text-disabled py-3 transition-all outline-none",
                            icon ? "pl-10 pr-4" : "px-4",
                            isFocused ? "border-accent-cyan shadow-[0_0_15px_rgba(0,240,255,0.1)] border" : "border border-border-color",
                            error && "border-accent-orange shadow-[0_0_15px_rgba(255,94,0,0.1)]",
                            success && "border-accent-green shadow-[0_0_15px_rgba(57,255,20,0.1)]",
                            className
                        )}
                        {...props}
                    />
                </div>

                <AnimatePresence>
                    {error && (
                        <motion.p
                            initial={{ opacity: 0, y: -5 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -5 }}
                            className="text-xs font-medium text-accent-orange mt-0.5"
                        >
                            {error}
                        </motion.p>
                    )}
                </AnimatePresence>
            </div>
        );
    }
);

Input.displayName = "Input";
