"use client";

import React from "react";
import { useRouter, usePathname } from "next/navigation";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import {
    Heart,
    Star,
    MapPin,
    Sparkles,
    Download,
    ArrowRight,
    LayoutDashboard,
    ClipboardList,
    History,
} from "lucide-react";

// ─── Sidebar ──────────────────────────────────────────────────────────────────

const NAV_ITEMS = [
    { label: "Overview",         href: "/app/workspace",               icon: <LayoutDashboard className="w-4 h-4" /> },
    { label: "Wishlist",         href: "/app/workspace/wishlist",       icon: <Heart className="w-4 h-4" /> },
    { label: "Shortlists",       href: "/app/workspace/shortlists",     icon: <Star className="w-4 h-4" /> },
    { label: "Admission Tracker",href: "/app/workspace/tracker",        icon: <MapPin className="w-4 h-4" /> },
    { label: "Recommendations",  href: "/app/recommendations",          icon: <Sparkles className="w-4 h-4" /> },
    { label: "History",          href: "/app/recommendations",          icon: <History className="w-4 h-4" /> },
    { label: "Export Center",    href: "/app/workspace/export",         icon: <Download className="w-4 h-4" /> },
    { label: "Dashboard",        href: "/app/dashboard",                icon: <ClipboardList className="w-4 h-4" /> },
];

function WorkspaceSidebar() {
    const pathname = usePathname();
    const router = useRouter();
    return (
        <aside className="hidden md:flex flex-col gap-1 w-52 shrink-0">
            <div className="glass-sm border border-border-color/30 rounded-xs p-2 flex flex-col gap-0.5">
                {NAV_ITEMS.map(item => {
                    const active = pathname === item.href || (pathname.startsWith(item.href) && item.href !== "/app/workspace" && item.href !== "/app/recommendations" && item.href !== "/app/dashboard");
                    return (
                        <button key={item.label} onClick={() => router.push(item.href)}
                            className={`flex items-center gap-3 px-3 py-2.5 rounded-xs text-[11px] font-semibold transition-all cursor-pointer text-left w-full select-none
                                ${active ? "bg-white/5 text-white border border-border-color/30" : "text-text-secondary hover:text-white hover:bg-white/3"}`}>
                            <span className={active ? "text-accent-cyan" : "text-text-tertiary"}>{item.icon}</span>
                            {item.label}
                            {active && <span className="ml-auto w-1.5 h-1.5 rounded-full bg-accent-cyan shrink-0" />}
                        </button>
                    );
                })}
            </div>
        </aside>
    );
}

// ─── Workspace Layout ─────────────────────────────────────────────────────────

export function WorkspaceLayout({ children }: { children: React.ReactNode }) {
    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white">
                <Navbar />
                <div className="pt-24 pb-16 px-6 max-w-7xl mx-auto flex gap-6 items-start">
                    <WorkspaceSidebar />
                    <main className="flex-1 min-w-0">{children}</main>
                </div>
            </div>
        </ProtectedRoute>
    );
}

// ─── Workspace Overview ───────────────────────────────────────────────────────

export default function WorkspacePage() {
    const router = useRouter();
    const quickLinks = [
        { label: "Wishlist", desc: "Browse and manage your saved colleges.", href: "/app/workspace/wishlist", icon: <Heart className="w-5 h-5 text-accent-orange" />, badge: "Manage" },
        { label: "Shortlists", desc: "Track priority choices with notes and editable rankings.", href: "/app/workspace/shortlists", icon: <Star className="w-5 h-5 text-accent-yellow" />, badge: "Organise" },
        { label: "Admission Tracker", desc: "Visual timeline of your application journey.", href: "/app/workspace/tracker", icon: <MapPin className="w-5 h-5 text-accent-cyan" />, badge: "Track" },
        { label: "Export Center", desc: "Download wishlist, shortlists, and reports.", href: "/app/workspace/export", icon: <Download className="w-5 h-5 text-accent-green" />, badge: "Export" },
    ];

    return (
        <WorkspaceLayout>
            {/* Ambient */}
            <div className="fixed inset-0 opacity-[0.015] pointer-events-none z-0">
                <div className="absolute top-[20%] left-[30%] w-96 h-96 bg-accent-cyan rounded-full blur-[160px]" />
                <div className="absolute bottom-[20%] right-[20%] w-80 h-80 bg-accent-purple rounded-full blur-[140px]" />
            </div>

            <div className="flex flex-col gap-10 relative z-10">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="cyan" glow className="w-fit text-[9px] px-2.5 py-1">
                            <LayoutDashboard className="w-3 h-3 mr-1.5 inline" /> Student Workspace
                        </Badge>
                        <h1 className="text-3xl md:text-5xl font-extrabold text-white tracking-tight">
                            Your Admission{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan to-accent-purple">
                                Operating System
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary max-w-xl leading-relaxed mt-1">
                            Manage your complete college admission journey — wishlist, shortlists, application tracking, and exports — in one seamless workspace.
                        </p>
                    </div>
                </ScrollReveal>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    {quickLinks.map((link, i) => (
                        <ScrollReveal key={link.label} delay={i * 0.07}>
                            <Card className="flex flex-col gap-4 p-6 text-left cursor-pointer group" onClick={() => router.push(link.href)}>
                                <div className="flex justify-between items-start">
                                    <div className="w-10 h-10 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center">
                                        {link.icon}
                                    </div>
                                    <Badge variant="default" className="text-[9px]">{link.badge}</Badge>
                                </div>
                                <div className="flex flex-col gap-1.5">
                                    <h2 className="font-bold text-sm text-white">{link.label}</h2>
                                    <p className="text-[11px] text-text-secondary leading-relaxed">{link.desc}</p>
                                </div>
                                <Button variant="secondary" size="sm" className="w-fit gap-1.5">
                                    Open <ArrowRight className="w-3.5 h-3.5" />
                                </Button>
                            </Card>
                        </ScrollReveal>
                    ))}
                </div>
            </div>
        </WorkspaceLayout>
    );
}
