"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { GraduationCap, User } from "lucide-react";
import { Button } from "../ui/Button";
import { CommandPalette } from "../dashboard/CommandPalette";

export function Navbar() {
    const router = useRouter();
    const [isCommandOpen, setIsCommandOpen] = useState(false);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if ((e.ctrlKey || e.metaKey) && e.key === "k") {
                e.preventDefault();
                setIsCommandOpen(true);
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, []);

    return (
        <>
            <motion.nav
                initial={{ opacity: 0, y: -20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
                className="fixed top-0 left-0 w-full z-40 glass-navbar px-6 py-4 flex items-center justify-between"
            >
                <div className="flex items-center gap-2 select-none cursor-pointer" onClick={() => router.push("/app/dashboard")}>
                    <GraduationCap className="w-5 h-5 text-white shrink-0" />
                    <span className="font-futuristic font-extralight text-sm tracking-[0.25em] text-white uppercase">
                        CampusSeekers
                    </span>
                </div>

                <div className="hidden md:flex items-center gap-8 text-xs font-semibold text-text-secondary select-none">
                    <span onClick={() => router.push("/app/dashboard")} className="hover:text-white transition-colors cursor-pointer">Dashboard</span>
                    <span onClick={() => router.push("/app/search")} className="hover:text-white transition-colors cursor-pointer">Explore</span>
                    <span onClick={() => router.push("/app/recommendations")} className="hover:text-white transition-colors cursor-pointer">Recommendations</span>
                    <span onClick={() => router.push("/app/workspace")} className="hover:text-white transition-colors cursor-pointer">Workflow</span>
                </div>

                <div className="flex items-center gap-4">
                    <Button variant="secondary" size="sm" onClick={() => router.push("/app/profile")}>
                        <User className="w-3.5 h-3.5" />
                        <span>Profile</span>
                    </Button>
                </div>
            </motion.nav>

            <CommandPalette isOpen={isCommandOpen} onClose={() => setIsCommandOpen(false)} />
        </>
    );
}
