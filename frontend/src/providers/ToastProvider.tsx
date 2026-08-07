"use client";

import React, { createContext, useContext, useState, useCallback } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { X, CheckCircle, AlertTriangle, Info, AlertOctagon } from "lucide-react";

export type ToastType = "success" | "error" | "info" | "warning";

export interface Toast {
    id: string;
    message: string;
    type: ToastType;
}

interface ToastContextType {
    show: (message: string, type: ToastType) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export function ToastProvider({ children }: { children: React.ReactNode }) {
    const [toasts, setToasts] = useState<Toast[]>([]);

    const show = useCallback((message: string, type: ToastType) => {
        const id = Math.random().toString(36).substring(2, 9);
        setToasts((prev) => [...prev, { id, message, type }]);

        setTimeout(() => {
            setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 4000);
    }, []);

    const remove = useCallback((id: string) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    return (
        <ToastContext.Provider value={{ show }}>
            {children}
            <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none">
                <AnimatePresence>
                    {toasts.map((toast) => (
                        <motion.div
                            key={toast.id}
                            layout
                            initial={{ opacity: 0, y: 50, scale: 0.9 }}
                            animate={{ opacity: 1, y: 0, scale: 1 }}
                            exit={{ opacity: 0, y: -20, scale: 0.95 }}
                            transition={{ type: "spring", stiffness: 300, damping: 25 }}
                            className="pointer-events-auto w-full glass-md rounded-md p-4 flex items-center justify-between gap-3 shadow-lg border border-border-color"
                        >
                            <div className="flex items-center gap-3">
                                {toast.type === "success" && <CheckCircle className="text-accent-green w-5 h-5 shrink-0" />}
                                {toast.type === "error" && <AlertOctagon className="text-accent-orange w-5 h-5 shrink-0" />}
                                {toast.type === "warning" && <AlertTriangle className="text-accent-purple w-5 h-5 shrink-0" />}
                                {toast.type === "info" && <Info className="text-accent-cyan w-5 h-5 shrink-0" />}
                                <p className="text-sm font-medium text-white select-none">{toast.message}</p>
                            </div>
                            <button
                                onClick={() => remove(toast.id)}
                                className="text-text-secondary hover:text-white transition-colors cursor-pointer"
                            >
                                <X className="w-4 h-4" />
                            </button>
                        </motion.div>
                    ))}
                </AnimatePresence>
            </div>
        </ToastContext.Provider>
    );
}

export function useToast() {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error("useToast must be used within a ToastProvider");
    }
    const show = context.show;
    return {
        success: (msg: string) => show(msg, "success"),
        error: (msg: string) => show(msg, "error"),
        warning: (msg: string) => show(msg, "warning"),
        info: (msg: string) => show(msg, "info"),
    };
}
