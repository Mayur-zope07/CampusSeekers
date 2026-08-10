"use client";

import React from "react";
import { usePathname, useRouter } from "next/navigation";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useProfile } from "@/hooks/useProfile";
import { useCurrentUser } from "@/hooks/useProfile";
import { useWishlist } from "@/hooks/useWorkflow";
import { useShortlists } from "@/hooks/useWorkflow";
import { useRecommendationHistory } from "@/hooks/useRecommendations";
import {
    User, GraduationCap, Shield, Settings, Activity,
    UserCircle, Sparkles,
    Heart, Star, MapPin, ChevronRight,
} from "lucide-react";

// ─── Sidebar Nav ──────────────────────────────────────────────────────────────

const NAV_SECTIONS = [
    { id: "overview",  label: "Profile Overview",   icon: <UserCircle className="w-4 h-4" />,    href: "/app/profile" },
    { id: "academic",  label: "Academic Profile",   icon: <User className="w-4 h-4" />,           href: "/app/profile/academic" },
    { id: "scores",    label: "Exam Scores",         icon: <GraduationCap className="w-4 h-4" />, href: "/app/profile/scores" },
    { id: "security",  label: "Security",            icon: <Shield className="w-4 h-4" />,         href: "/app/profile/security" },
    { id: "activity",  label: "Activity",            icon: <Activity className="w-4 h-4" />,       href: "/app/profile/activity" },
    { id: "settings",  label: "App Settings",        icon: <Settings className="w-4 h-4" />,       href: "/app/profile/settings" },
];

function ProfileSidebar() {
    const pathname = usePathname();
    const router = useRouter();
    return (
        <aside className="hidden md:flex flex-col gap-1 w-52 shrink-0">
            <div className="glass-sm border border-border-color/30 rounded-xs p-2 flex flex-col gap-0.5">
                {NAV_SECTIONS.map(item => {
                    const active = pathname === item.href;
                    return (
                        <button key={item.id} onClick={() => router.push(item.href)}
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

// ─── Layout ───────────────────────────────────────────────────────────────────

export function ProfileLayout({ children }: { children: React.ReactNode }) {
    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white">
                <Navbar />
                <div className="pt-24 pb-16 px-6 max-w-7xl mx-auto flex gap-6 items-start">
                    <ProfileSidebar />
                    <main className="flex-1 min-w-0">{children}</main>
                </div>
            </div>
        </ProtectedRoute>
    );
}

// ─── Completion Ring ──────────────────────────────────────────────────────────

function CompletionRing({ pct }: { pct: number }) {
    const r = 28;
    const circ = 2 * Math.PI * r;
    const dash = (pct / 100) * circ;
    return (
        <svg width="72" height="72" className="-rotate-90">
            <circle cx="36" cy="36" r={r} fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="5" />
            <circle cx="36" cy="36" r={r} fill="none" stroke="url(#ring-grad)" strokeWidth="5"
                strokeDasharray={`${dash} ${circ - dash}`} strokeLinecap="round" />
            <defs>
                <linearGradient id="ring-grad" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stopColor="#00f0ff" />
                    <stop offset="100%" stopColor="#8b2be2" />
                </linearGradient>
            </defs>
        </svg>
    );
}

// ─── Profile Overview Page ────────────────────────────────────────────────────

export default function ProfilePage() {
    const router = useRouter();
    const { data: profile, isLoading: profileLoading } = useProfile();
    const { data: user, isLoading: userLoading } = useCurrentUser();
    const { data: wishlist } = useWishlist();
    const { data: shortlists } = useShortlists();
    const { data: recHistory } = useRecommendationHistory();

    const isLoading = profileLoading || userLoading;

    const completionItems = [
        { label: "Basic Profile",   done: !!profile },
        { label: "Exam Scores",     done: false },
        { label: "Wishlist",        done: (wishlist?.length ?? 0) > 0 },
        { label: "Shortlist",       done: (shortlists?.length ?? 0) > 0 },
        { label: "Recommendation",  done: (recHistory?.length ?? 0) > 0 },
    ];
    const completionPct = Math.round((completionItems.filter(c => c.done).length / completionItems.length) * 100);

    const quickLinks = [
        { label: "Edit Academic Profile", desc: "Update name, category, home details.", href: "/app/profile/academic", icon: <User className="w-4 h-4 text-accent-cyan" /> },
        { label: "Manage Exam Scores",    desc: "Add or edit MHT-CET, JEE scores.",   href: "/app/profile/scores",   icon: <GraduationCap className="w-4 h-4 text-accent-purple" /> },
        { label: "Security Settings",     desc: "Review session and account info.",    href: "/app/profile/security", icon: <Shield className="w-4 h-4 text-accent-green" /> },
        { label: "Activity Timeline",     desc: "Browse your recent platform activity.", href: "/app/profile/activity", icon: <Activity className="w-4 h-4 text-accent-orange" /> },
    ];

    return (
        <ProfileLayout>
            <div className="flex flex-col gap-8">
                {/* Ambient */}
                <div className="fixed inset-0 opacity-[0.015] pointer-events-none z-0">
                    <div className="absolute top-[25%] right-[20%] w-80 h-80 bg-accent-cyan rounded-full blur-[140px]" />
                    <div className="absolute bottom-[30%] left-[25%] w-64 h-64 bg-accent-purple rounded-full blur-[120px]" />
                </div>

                {/* Header */}
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="cyan" glow className="w-fit text-[9px] px-2.5 py-1">
                            <UserCircle className="w-3 h-3 mr-1.5 inline" /> Account Center
                        </Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight">
                            Your{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan to-accent-purple">
                                Profile
                            </span>
                        </h1>
                    </div>
                </ScrollReveal>

                {isLoading ? (
                    <div className="flex flex-col gap-4">
                        <Skeleton className="h-36 w-full" />
                        <div className="grid grid-cols-3 gap-4">{[1, 2, 3].map(x => <Skeleton key={x} className="h-20" />)}</div>
                    </div>
                ) : (
                    <div className="flex flex-col gap-6">
                        {/* Identity Card */}
                        <ScrollReveal delay={0.04}>
                            <Card className="flex items-center gap-6 p-6 text-left" hoverLift={false} glowColor="rgba(0,240,255,0.04)">
                                {/* Avatar */}
                                <div className="relative shrink-0">
                                    <CompletionRing pct={completionPct} />
                                    <div className="absolute inset-0 flex items-center justify-center">
                                        <div className="w-11 h-11 rounded-full bg-gradient-to-br from-accent-cyan/30 to-accent-purple/30 border border-border-color/30 flex items-center justify-center">
                                            <span className="text-sm font-bold text-white">
                                                {profile ? `${profile.firstName[0]}${profile.lastName[0]}` : "?"}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                {/* Info */}
                                <div className="flex flex-col gap-1.5 min-w-0 flex-1">
                                    <div className="flex items-center gap-2 flex-wrap">
                                        <h2 className="text-lg font-bold text-white">
                                            {profile ? `${profile.firstName} ${profile.lastName}` : "Profile Incomplete"}
                                        </h2>
                                        <Badge variant="cyan" className="text-[9px]">{user?.role ?? "STUDENT"}</Badge>
                                    </div>
                                    <p className="text-[11px] text-text-secondary">{user?.email ?? "—"}</p>
                                    <p className="text-[10px] text-text-tertiary">
                                        {profile?.category && `Category: ${profile.category}`}
                                        {profile?.homeState && ` · ${profile.homeDistrict}, ${profile.homeState}`}
                                    </p>
                                </div>
                                {/* Completion */}
                                <div className="text-right shrink-0 hidden sm:flex flex-col gap-1">
                                    <span className="text-2xl font-bold font-futuristic text-white">{completionPct}%</span>
                                    <span className="text-[9px] text-text-tertiary uppercase tracking-wider">Profile Complete</span>
                                </div>
                            </Card>
                        </ScrollReveal>

                        {/* Quick Stats */}
                        <ScrollReveal delay={0.07}>
                            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                                {[
                                    { label: "Wishlist",        value: wishlist?.length ?? 0,   icon: <Heart className="w-3.5 h-3.5 text-accent-orange" /> },
                                    { label: "Shortlists",      value: shortlists?.filter(s => !s.isDeleted).length ?? 0, icon: <Star className="w-3.5 h-3.5 text-accent-yellow" /> },
                                    { label: "AI Runs",         value: recHistory?.length ?? 0, icon: <Sparkles className="w-3.5 h-3.5 text-accent-purple" /> },
                                    { label: "Home District",   value: profile?.homeDistrict ?? "—", icon: <MapPin className="w-3.5 h-3.5 text-accent-cyan" /> },
                                ].map((s, i) => (
                                    <Card key={i} className="flex flex-col gap-1.5 p-4" hoverLift={false}>
                                        <div className="flex items-center justify-between">
                                            <span className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">{s.label}</span>
                                            {s.icon}
                                        </div>
                                        <span className="text-xl font-light font-futuristic text-white">{s.value}</span>
                                    </Card>
                                ))}
                            </div>
                        </ScrollReveal>

                        {/* Profile Completion Checklist */}
                        <ScrollReveal delay={0.1}>
                            <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
                                <div className="flex items-center justify-between">
                                    <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Profile Checklist</span>
                                    <Badge variant={completionPct === 100 ? "green" : "cyan"} className="text-[9px]">{completionPct}% done</Badge>
                                </div>
                                <div className="flex flex-col gap-2">
                                    {completionItems.map((item) => (
                                        <div key={item.label} className="flex items-center gap-3 text-[11px]">
                                            <div className={`w-4 h-4 rounded-full border-2 flex items-center justify-center shrink-0 ${item.done ? "border-accent-green bg-accent-green/20" : "border-border-color bg-white/3"}`}>
                                                {item.done && <div className="w-1.5 h-1.5 rounded-full bg-accent-green" />}
                                            </div>
                                            <span className={item.done ? "text-white" : "text-text-tertiary"}>{item.label}</span>
                                            {!item.done && <span className="ml-auto text-[9px] text-accent-cyan cursor-pointer select-none">Setup →</span>}
                                        </div>
                                    ))}
                                </div>
                            </Card>
                        </ScrollReveal>

                        {/* Quick Actions */}
                        <ScrollReveal delay={0.13}>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                {quickLinks.map((link) => (
                                    <button key={link.label} onClick={() => router.push(link.href)}
                                        className="flex items-center gap-4 p-4 glass-sm border border-border-color/30 rounded-xs hover:border-accent-cyan/30 transition-all cursor-pointer text-left group">
                                        <div className="w-9 h-9 rounded-xs border border-border-color/20 bg-white/3 flex items-center justify-center shrink-0">
                                            {link.icon}
                                        </div>
                                        <div className="flex flex-col gap-0.5 flex-1 min-w-0">
                                            <span className="text-[11px] font-bold text-white">{link.label}</span>
                                            <span className="text-[10px] text-text-tertiary">{link.desc}</span>
                                        </div>
                                        <ChevronRight className="w-4 h-4 text-text-tertiary group-hover:text-accent-cyan transition-colors shrink-0" />
                                    </button>
                                ))}
                            </div>
                        </ScrollReveal>
                    </div>
                )}
            </div>
        </ProfileLayout>
    );
}
