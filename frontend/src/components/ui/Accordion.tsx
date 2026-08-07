"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronDown } from "lucide-react";
import { cn } from "@/utils/cn";

export interface AccordionItem {
    id: string;
    title: string;
    content: React.ReactNode;
}

interface AccordionProps {
    items: AccordionItem[];
    className?: string;
}

export function Accordion({ items, className }: AccordionProps) {
    const [openId, setOpenId] = useState<string | null>(null);

    const toggle = (id: string) => {
        setOpenId((prev) => (prev === id ? null : id));
    };

    return (
        <div className={cn("flex flex-col border border-border-color rounded-md overflow-hidden glass-sm", className)}>
            {items.map((item, idx) => {
                const isOpen = item.id === openId;
                const isLast = idx === items.length - 1;

                return (
                    <div key={item.id} className={cn("flex flex-col", !isLast && "border-b border-border-color")}>
                        <button
                            onClick={() => toggle(item.id)}
                            className="flex items-center justify-between px-5 py-4 w-full text-sm text-left font-semibold text-white hover:bg-white/5 transition-all select-none cursor-pointer"
                        >
                            <span>{item.title}</span>
                            <motion.span
                                animate={{ rotate: isOpen ? 180 : 0 }}
                                transition={{ type: "spring", damping: 20, stiffness: 200 }}
                            >
                                <ChevronDown className="w-4 h-4 text-text-secondary" />
                            </motion.span>
                        </button>

                        <AnimatePresence initial={false}>
                            {isOpen && (
                                <motion.div
                                    initial={{ height: 0, opacity: 0 }}
                                    animate={{ height: "auto", opacity: 1 }}
                                    exit={{ height: 0, opacity: 0 }}
                                    transition={{ type: "spring", damping: 30, stiffness: 250 }}
                                >
                                    <div className="px-5 pb-5 pt-1 text-xs text-text-secondary leading-relaxed">
                                        {item.content}
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                );
            })}
        </div>
    );
}
