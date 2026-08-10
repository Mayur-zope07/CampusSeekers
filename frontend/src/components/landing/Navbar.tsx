"use client";

import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { GraduationCap, Menu, X } from "lucide-react";
import { Button } from "../ui/Button";
import { Magnetic } from "../animations/Magnetic";
import { cn } from "@/utils/cn";

const navLinks = [
    { label: "Home", href: "#home", id: "home" },
    { label: "Features", href: "#features", id: "features" },
    { label: "How It Works", href: "#how-it-works", id: "how-it-works" },
    { label: "Recommendations", href: "#recommendations", id: "recommendations" },
];

export function Navbar() {
    const [isScrolled, setIsScrolled] = useState(false);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [activeSection, setActiveSection] = useState("home");

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 20);
        };
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    useEffect(() => {
        const sections = navLinks.map((link) => document.getElementById(link.id));
        const observerOptions = {
            root: null,
            rootMargin: "-40% 0px -50% 0px",
            threshold: 0,
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    setActiveSection(entry.target.id);
                }
            });
        }, observerOptions);

        sections.forEach((section) => {
            if (section) observer.observe(section);
        });

        return () => {
            sections.forEach((section) => {
                if (section) observer.unobserve(section);
            });
        };
    }, []);

    return (
        <header className="fixed top-0 left-0 w-full z-50 px-6 pt-6 transition-all duration-300">
            <div
                className={cn(
                    "max-w-5xl mx-auto rounded-md transition-all duration-300 flex items-center justify-between border border-transparent",
                    isScrolled
                        ? "glass-navbar px-6 py-3 border-border-color shadow-lg scale-98"
                        : "bg-transparent px-4 py-4"
                )}
            >
                <div className="flex items-center gap-2 select-none shrink-0">
                    <GraduationCap className="w-5 h-5 text-white animate-pulse" />
                    <span className="font-futuristic font-extralight text-sm tracking-[0.25em] text-white uppercase">
                        CampusSeekers
                    </span>
                </div>

                <div className="hidden md:flex items-center gap-8 text-[11px] font-bold text-text-secondary select-none">
                    {navLinks.map((link) => (
                        <a
                            key={link.id}
                            href={link.href}
                            className={cn(
                                "hover:text-white transition-colors uppercase tracking-wider relative py-1",
                                activeSection === link.id ? "text-white font-semibold" : ""
                            )}
                        >
                            {link.label}
                            {activeSection === link.id && (
                                <motion.div
                                    layoutId="active-nav-underline"
                                    className="absolute bottom-0 left-0 w-full h-[1px] bg-accent-cyan"
                                    transition={{ type: "spring", stiffness: 350, damping: 30 }}
                                />
                            )}
                        </a>
                    ))}
                </div>

                <div className="hidden md:flex items-center gap-3 shrink-0">
                    <span className="text-xs font-semibold text-text-secondary hover:text-white transition-colors cursor-pointer mr-2 select-none">
                        Login
                    </span>
                    <Magnetic>
                        <Button variant="primary" size="sm">
                            Get Started
                        </Button>
                    </Magnetic>
                </div>

                <button
                    onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    className="md:hidden text-white hover:text-text-secondary transition-colors cursor-pointer shrink-0"
                >
                    {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                </button>
            </div>

            <AnimatePresence>
                {isMobileMenuOpen && (
                    <motion.div
                        initial={{ opacity: 0, y: -10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                        className="md:hidden absolute top-full left-0 w-full px-6 mt-2"
                    >
                        <div className="glass-dialog border border-border-color rounded-md p-6 flex flex-col gap-4 shadow-2xl">
                            {navLinks.map((link) => (
                                <a
                                    key={link.id}
                                    href={link.href}
                                    onClick={() => setIsMobileMenuOpen(false)}
                                    className="text-sm font-semibold text-text-secondary hover:text-white transition-colors"
                                >
                                    {link.label}
                                </a>
                            ))}
                            <div className="h-[1px] bg-border-color/50 my-2" />
                            <div className="flex flex-col gap-3">
                                <Button variant="secondary" className="w-full">
                                    Login
                                </Button>
                                <Button variant="primary" className="w-full">
                                    Get Started
                                </Button>
                            </div>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </header>
    );
}
