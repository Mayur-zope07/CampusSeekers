"use client";

import React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X } from "lucide-react";
import { cn } from "@/utils/cn";

interface DrawerProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: React.ReactNode;
    placement?: "right" | "bottom";
    className?: string;
}

export function Drawer({
    isOpen,
    onClose,
    title,
    children,
    placement = "right",
    className,
}: DrawerProps) {
    const isRight = placement === "right";

    const slideVariants = {
        initial: isRight ? { x: "100%", y: 0 } : { x: 0, y: "100%" },
        animate: { x: 0, y: 0 },
        exit: isRight ? { x: "100%", y: 0 } : { x: 0, y: "100%" },
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={onClose}
                        className="fixed inset-0 bg-black/60 backdrop-blur-[4px] z-50 pointer-events-auto"
                    />

                    <motion.div
                        variants={slideVariants}
                        initial="initial"
                        animate="animate"
                        exit="exit"
                        transition={{ type: "spring", damping: 30, stiffness: 300 }}
                        className={cn(
                            "fixed glass-dialog border-border-color z-50 pointer-events-auto shadow-2xl flex flex-col p-6",
                            isRight
                                ? "top-0 right-0 h-full w-full max-w-md border-l"
                                : "bottom-0 left-0 w-full h-[60vh] border-t rounded-t-xl",
                            className
                        )}
                    >
                        <div className="flex items-center justify-between mb-6 pb-4 border-b border-border-color/30">
                            {title && <h2 className="text-md font-bold text-white tracking-wide select-none">{title}</h2>}
                            <button
                                onClick={onClose}
                                className="text-text-secondary hover:text-white transition-colors cursor-pointer"
                            >
                                <X className="w-5 h-5" />
                            </button>
                        </div>

                        <div className="flex-1 overflow-y-auto pr-2 text-sm text-text-secondary leading-relaxed">
                            {children}
                        </div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
}
