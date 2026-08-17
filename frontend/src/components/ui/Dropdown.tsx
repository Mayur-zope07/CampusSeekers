"use client";

import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronDown } from "lucide-react";
import { cn } from "@/utils/cn";

export interface DropdownOption {
    label: string;
    value: string;
    icon?: React.ReactNode;
}

interface DropdownProps {
    options: DropdownOption[];
    selected?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
    className?: string;
}

export function Dropdown({
    options,
    selected,
    onChange,
    placeholder = "Select Option",
    className,
}: DropdownProps) {
    const [isOpen, setIsOpen] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);

    const activeOption = options.find((o) => o.value === selected);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <div ref={containerRef} className={cn("relative w-full", isOpen && "z-50", className)}>
            <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex items-center justify-between glass-sm rounded-sm px-4 py-3 text-sm text-white border border-border-color hover:border-white/15 focus:border-accent-cyan outline-none transition-all cursor-pointer select-none"
            >
                <div className="flex items-center gap-2">
                    {activeOption?.icon && <span className="text-text-secondary shrink-0">{activeOption.icon}</span>}
                    <span>{activeOption ? activeOption.label : placeholder}</span>
                </div>
                <ChevronDown
                    className={cn("w-4 h-4 text-text-secondary transition-transform duration-300", isOpen && "rotate-180")}
                />
            </button>

            <AnimatePresence>
                {isOpen && (
                    <motion.ul
                        initial={{ opacity: 0, y: 10, scale: 0.98 }}
                        animate={{ opacity: 1, y: 4, scale: 1 }}
                        exit={{ opacity: 0, y: 10, scale: 0.98 }}
                        transition={{ duration: 0.15, ease: "easeOut" }}
                        className="absolute left-0 w-full glass-md border border-border-color rounded-sm py-1.5 shadow-2xl z-40 max-h-60 overflow-y-auto"
                    >
                        {options.map((option) => (
                            <li key={option.value}>
                                <button
                                    type="button"
                                    onClick={() => {
                                        onChange?.(option.value);
                                        setIsOpen(false);
                                    }}
                                    className={cn(
                                        "w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-left text-text-secondary hover:text-white hover:bg-white/5 transition-all select-none cursor-pointer",
                                        option.value === selected && "text-white bg-white/5 font-semibold border-l-2 border-accent-cyan pl-[14px]"
                                    )}
                                >
                                    {option.icon && <span className="shrink-0">{option.icon}</span>}
                                    <span>{option.label}</span>
                                </button>
                            </li>
                        ))}
                    </motion.ul>
                )}
            </AnimatePresence>
        </div>
    );
}
