"use client";

import React from "react";
import { ProfileLayout } from "@/app/app/profile/page";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useRecommendationHistory } from "@/hooks/useRecommendations";
import { useWishlist } from "@/hooks/useWorkflow";
import { useShortlists } from "@/hooks/useWorkflow";
import { Activity, Sparkles, Heart, Star, ArrowRight, Clock } from "lucide-react";
import { useRouter } from "next/navigation";

interface ActivityEntry {
    id: string;
    type: "recommendation" | "wishlist" | "shortlist";
    label: string;
    sub: string;
    date: string;
    href: string;
    icon: React.ReactNode;
    color: string;
}

export default function ActivityPage() {
    const router = useRouter();
    const { data: recHistory, isLoading: recLoading } = useRecommendationHistory();
    const { data: wishlist, isLoading: wishLoading } = useWishlist();
    const { data: shortlists, isLoading: shortLoading } = useShortlists();

    const isLoading = recLoading || wishLoading || shortLoading;

    const entries: ActivityEntry[] = [
        ...(recHistory ?? []).map(r => ({
            id: r.id,
            type: "recommendation" as const,
            label: `AI Recommendation — ${r.examName.replace("_", " ")}`,
            sub: `${Number(r.percentile).toFixed(2)}%ile · ${r.returnedCount} results · ${r.category}`,
            date: r.createdAt,
            href: `/app/recommendations/${r.id}`,
            icon: <Sparkles className="w-3.5 h-3.5" />,
            color: "text-accent-purple bg-accent-purple/10 border-accent-purple/20",
        })),
        ...(wishlist ?? []).map(w => ({
            id: w.id,
            type: "wishlist" as const,
            label: `Wishlisted — ${w.collegeName}`,
            sub: `${w.city}, ${w.state}${w.naacGrade ? ` · NAAC ${w.naacGrade}` : ""}`,
            date: w.createdAt,
            href: `/app/colleges/${w.collegeId}`,
            icon: <Heart className="w-3.5 h-3.5" />,
            color: "text-accent-orange bg-accent-orange/10 border-accent-orange/20",
        })),
        ...(shortlists ?? []).filter(s => !s.isDeleted).map(s => ({
            id: s.id,
            type: "shortlist" as const,
            label: `Shortlisted — ${s.collegeName}`,
            sub: `${s.branchName} · Priority ${s.priority ?? "—"}`,
            date: s.addedAt,
            href: `/app/workspace/shortlists`,
            icon: <Star className="w-3.5 h-3.5" />,
            color: "text-accent-yellow bg-accent-yellow/10 border-accent-yellow/20",
        })),
    ].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

    return (
        <ProfileLayout>
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left">
                        <Badge variant="orange" className="w-fit text-[9px] px-2.5 py-1">
                            <Activity className="w-3 h-3 mr-1.5 inline" /> Activity
                        </Badge>
                        <h1 className="text-2xl md:text-3xl font-extrabold text-white">Activity Timeline</h1>
                        <p className="text-xs text-text-secondary max-w-md">Chronological feed of your recommendations, wishlist, and shortlist activity.</p>
                    </div>
                </ScrollReveal>

                {isLoading ? (
                    <div className="flex flex-col gap-3">{[1, 2, 3, 4, 5].map(x => <Skeleton key={x} className="h-16 w-full" />)}</div>
                ) : entries.length > 0 ? (
                    <div className="flex flex-col gap-0 relative">
                        {/* Vertical line */}
                        <div className="absolute left-[17px] top-5 bottom-5 w-px bg-border-color/20" />

                        {entries.map((entry, i) => {
                            const date = new Date(entry.date).toLocaleDateString("en-IN", {
                                day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
                            });
                            return (
                                <ScrollReveal key={entry.id} delay={i * 0.03}>
                                    <button onClick={() => router.push(entry.href)}
                                        className="relative flex items-start gap-4 pb-5 text-left w-full group cursor-pointer">
                                        {/* Dot */}
                                        <div className={`w-[18px] h-[18px] rounded-full border flex items-center justify-center shrink-0 mt-1 ${entry.color} z-10`}>
                                            {entry.icon}
                                        </div>
                                        {/* Content */}
                                        <div className="flex-1 min-w-0 flex items-start justify-between gap-3 glass-sm border border-border-color/20 rounded-xs px-3 py-3 group-hover:border-border-color/40 transition-colors">
                                            <div className="flex flex-col gap-0.5 min-w-0">
                                                <span className="text-[11px] font-semibold text-white truncate">{entry.label}</span>
                                                <span className="text-[10px] text-text-tertiary">{entry.sub}</span>
                                                <span className="text-[9px] text-text-tertiary flex items-center gap-1 mt-0.5">
                                                    <Clock className="w-2.5 h-2.5" /> {date}
                                                </span>
                                            </div>
                                            <ArrowRight className="w-3.5 h-3.5 text-text-tertiary group-hover:text-white transition-colors shrink-0 mt-1" />
                                        </div>
                                    </button>
                                </ScrollReveal>
                            );
                        })}
                    </div>
                ) : (
                    <EmptyState
                        title="No activity yet"
                        description="Your recommendation, wishlist, and shortlist activity will appear here as a chronological timeline."
                    />
                )}
            </div>
        </ProfileLayout>
    );
}
