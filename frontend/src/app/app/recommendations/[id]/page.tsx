"use client";

import React, { use } from "react";
import { useRouter } from "next/navigation";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import { useRecommendation, useShortlistRecommendation, type RecommendationItem } from "@/hooks/useRecommendations";
import {
    ArrowLeft,
    Brain,
    Sparkles,
    Star,
    ArrowRight,
    CheckCircle,
    AlertCircle,
    Zap,
    BookOpen,
} from "lucide-react";
import { useState } from "react";

// ─── Helpers ──────────────────────────────────────────────────────────────────

const CATEGORY_STYLES: Record<string, { variant: "green" | "cyan" | "purple"; border: string }> = {
    SAFE:   { variant: "green",  border: "border-accent-green/20" },
    TARGET: { variant: "cyan",   border: "border-accent-cyan/20" },
    DREAM:  { variant: "purple", border: "border-accent-purple/20" },
};

// ─── Page ─────────────────────────────────────────────────────────────────────

interface PageProps { params: Promise<{ id: string }> }

export default function RecommendationDetailsPage({ params }: PageProps) {
    const router = useRouter();
    const toast = useToast();
    const { id } = use(params);

    const { data: rec, isLoading } = useRecommendation(id);
    const { mutate: shortlist } = useShortlistRecommendation();
    const [shortlistingId, setShortlistingId] = useState<string | null>(null);

    const handleShortlist = (itemId: string) => {
        setShortlistingId(itemId);
        shortlist(itemId, {
            onSuccess: () => { toast.success("Added to shortlist!"); setShortlistingId(null); },
            onError:   () => { toast.error("Failed to shortlist."); setShortlistingId(null); },
        });
    };

    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-64 pt-24 pb-16 px-6 relative overflow-hidden">
                <Navbar />
                <Sidebar activeItem="recommendations" onChange={() => {}} />

                <div className="max-w-4xl mx-auto flex flex-col gap-8 relative z-10">
                    {/* Back */}
                    <button
                        onClick={() => router.push("/app/recommendations")}
                        className="flex items-center gap-2 text-xs text-text-secondary hover:text-white transition-colors cursor-pointer select-none w-fit"
                    >
                        <ArrowLeft className="w-4 h-4" /> Back to Recommendations
                    </button>

                    {isLoading ? (
                        <div className="flex flex-col gap-4">
                            <Skeleton className="h-12 w-2/3" />
                            <div className="grid grid-cols-3 gap-4">{[1,2,3,4,5,6].map(x => <Skeleton key={x} className="h-20" />)}</div>
                            {[1,2,3].map(x => <Skeleton key={x} className="h-36 w-full" />)}
                        </div>
                    ) : rec ? (
                        <div className="flex flex-col gap-8 text-left">
                            {/* Header */}
                            <ScrollReveal>
                                <div className="flex flex-col gap-3">
                                    <div className="flex flex-wrap items-center gap-2">
                                        <Badge variant="purple" glow className="text-[9px] px-2.5 py-1">
                                            <Brain className="w-3 h-3 mr-1.5 inline" /> AI Engine · v{rec.engineVersion}
                                        </Badge>
                                        {rec.cacheHit && <Badge variant="cyan" className="text-[9px] px-2 py-0.5">Cache Hit</Badge>}
                                    </div>
                                    <h1 className="text-2xl md:text-4xl font-extrabold text-white">
                                        {rec.examName.replace("_", " ")} ·{" "}
                                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-purple to-accent-cyan">
                                            {Number(rec.percentile).toFixed(2)} %ile
                                        </span>
                                    </h1>
                                    <p className="text-xs text-text-secondary">
                                        Category: <strong className="text-white">{rec.category}</strong> ·
                                        Year: <strong className="text-white">{rec.admissionYear}</strong> ·
                                        Generated: <strong className="text-white">{new Date(rec.createdAt).toLocaleString("en-IN")}</strong>
                                    </p>
                                </div>
                            </ScrollReveal>

                            {/* Statistics grid */}
                            <ScrollReveal delay={0.05}>
                                <div className="grid grid-cols-3 md:grid-cols-6 gap-3">
                                    {[
                                        { label: "Evaluated", value: rec.evaluatedCount, icon: <BookOpen className="w-3.5 h-3.5 text-text-secondary" /> },
                                        { label: "Returned", value: rec.returnedCount,  icon: <Sparkles className="w-3.5 h-3.5 text-accent-cyan" /> },
                                        { label: "Safe",     value: rec.safeCount,      icon: <CheckCircle className="w-3.5 h-3.5 text-accent-green" /> },
                                        { label: "Target",   value: rec.targetCount,    icon: <AlertCircle className="w-3.5 h-3.5 text-accent-cyan" /> },
                                        { label: "Dream",    value: rec.dreamCount,     icon: <Sparkles className="w-3.5 h-3.5 text-accent-purple" /> },
                                        { label: "Time",     value: `${rec.executionTimeMs}ms`, icon: <Zap className="w-3.5 h-3.5 text-accent-orange" /> },
                                    ].map((s, i) => (
                                        <Card key={i} className="flex flex-col gap-1.5 p-3" hoverLift={false}>
                                            <div className="flex justify-between items-center">
                                                <span className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">{s.label}</span>
                                                {s.icon}
                                            </div>
                                            <span className="text-lg font-light font-futuristic text-white">{s.value}</span>
                                        </Card>
                                    ))}
                                </div>
                            </ScrollReveal>

                            {/* Applied filters */}
                            {(rec.preferredBranches?.length > 0 || rec.preferredCities?.length > 0 || rec.minimumNaac || rec.maximumFees) && (
                                <ScrollReveal delay={0.08}>
                                    <Card className="flex flex-col gap-3 p-4" hoverLift={false}>
                                        <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Applied Filters</span>
                                        <div className="flex flex-wrap gap-2">
                                            {rec.preferredBranches?.map(b => <Badge key={b} variant="default" className="text-[10px]">{b}</Badge>)}
                                            {rec.preferredCities?.map(c => <Badge key={c} variant="cyan" className="text-[10px]">{c}</Badge>)}
                                            {rec.minimumNaac && <Badge variant="purple" className="text-[10px]">NAAC ≥ {rec.minimumNaac}</Badge>}
                                            {rec.maximumFees && <Badge variant="default" className="text-[10px]">Max ₹{(Number(rec.maximumFees)/1000).toFixed(0)}K/yr</Badge>}
                                        </div>
                                    </Card>
                                </ScrollReveal>
                            )}

                            {/* Recommendation Items */}
                            <div className="flex flex-col gap-4">
                                {rec.items.map((item: RecommendationItem, idx: number) => {
                                    const style = CATEGORY_STYLES[item.recommendationCategory] ?? { variant: "default" as const, border: "" };
                                    const diff  = Number(item.percentileDifference ?? 0);
                                    return (
                                        <ScrollReveal key={`${item.branchId}-${idx}`} delay={idx * 0.04}>
                                            <Card className={`flex flex-col gap-4 p-5 text-left border ${style.border}`} hoverLift={false}>
                                                {/* Card Header */}
                                                <div className="flex justify-between items-start gap-3">
                                                    <div className="flex flex-col gap-1 min-w-0">
                                                        <div className="flex items-center gap-2 flex-wrap">
                                                            <Badge variant={style.variant} className="text-[9px] px-2 py-0.5 shrink-0">{item.recommendationCategory}</Badge>
                                                            <span className="font-bold text-sm text-white">{item.collegeName}</span>
                                                        </div>
                                                        <span className="text-[11px] text-text-tertiary">{item.branchName} · {item.city}</span>
                                                    </div>
                                                    <div className="text-right shrink-0">
                                                        <span className={`text-sm font-bold ${diff > 0 ? "text-accent-green" : diff < 0 ? "text-accent-orange" : "text-text-secondary"}`}>
                                                            {diff > 0 ? `+${diff.toFixed(2)}` : diff.toFixed(2)}%
                                                        </span>
                                                        <div className="text-[9px] text-text-tertiary">vs closing</div>
                                                    </div>
                                                </div>

                                                {/* AI Explanation */}
                                                {item.humanReadableReason && (
                                                    <div className="flex items-start gap-2.5 px-3 py-2.5 rounded-xs border border-border-color/30 bg-white/2 text-[11px] text-text-secondary">
                                                        <Brain className="w-3.5 h-3.5 shrink-0 mt-0.5 text-accent-purple" />
                                                        <span className="leading-relaxed">{item.humanReadableReason}</span>
                                                    </div>
                                                )}

                                                {/* Stats */}
                                                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-[11px]">
                                                    <div><span className="text-text-tertiary block">Closing %ile</span><span className="font-semibold text-white">{Number(item.closingPercentile).toFixed(2)}</span></div>
                                                    <div><span className="text-text-tertiary block">Avg Package</span><span className="font-semibold text-accent-green">{item.averagePackage ? `₹${(Number(item.averagePackage)/100000).toFixed(1)}L` : "N/A"}</span></div>
                                                    <div><span className="text-text-tertiary block">Fees/Year</span><span className="font-semibold text-white">{item.feesPerYear ? `₹${(Number(item.feesPerYear)/1000).toFixed(0)}K` : "N/A"}</span></div>
                                                    <div><span className="text-text-tertiary block">NAAC</span><span className="font-semibold text-white">{item.naacGrade || "A"}</span></div>
                                                </div>

                                                {/* Actions */}
                                                <div className="flex gap-2 pt-1 border-t border-border-color/20">
                                                    <Button variant="secondary" size="sm" className="flex-1" onClick={() => router.push(`/app/colleges/${item.collegeId}`)}>
                                                        <ArrowRight className="w-3.5 h-3.5 mr-1" /> View College
                                                    </Button>
                                                    <Button variant="primary" size="sm" className="flex-1" isLoading={shortlistingId === item.id} onClick={() => handleShortlist(item.id)}>
                                                        <Star className="w-3.5 h-3.5 mr-1" /> Shortlist
                                                    </Button>
                                                </div>
                                            </Card>
                                        </ScrollReveal>
                                    );
                                })}
                            </div>

                            {rec.items.length === 0 && (
                                <EmptyState title="No items in this recommendation" description="This recommendation session returned no matching colleges for the given parameters." />
                            )}
                        </div>
                    ) : (
                        <div className="py-20">
                            <EmptyState title="Recommendation not found" description="This recommendation session no longer exists or you don't have access to it." />
                        </div>
                    )}
                </div>
            </div>
        </ProtectedRoute>
    );
}
