"use client";

import React, { useState } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Dropdown } from "@/components/ui/Dropdown";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import {
    useGenerateRecommendation,
    useRecommendationHistory,
    useShortlistRecommendation,
    type RecommendationItem,
    type RecommendationResult,
    type RecommendationHistory,
} from "@/hooks/useRecommendations";
import {
    Sparkles,
    Brain,
    TrendingUp,
    Star,
    ArrowRight,
    CheckCircle,
    AlertCircle,
    Zap,
    BookOpen,
    PlusCircle,
} from "lucide-react";
import { useRouter } from "next/navigation";

// ─── Recommendation Card ──────────────────────────────────────────────────────

function RecommendationCard({
    item,
    onShortlist,
    isShortlisting,
    onView,
}: {
    item: RecommendationItem;
    onShortlist: (id: string) => void;
    isShortlisting: boolean;
    onView: (id: string) => void;
}) {
    const categoryMap: Record<string, { variant: "green" | "cyan" | "purple"; label: string }> = {
        SAFE: { variant: "green", label: "SAFE" },
        TARGET: { variant: "cyan", label: "TARGET" },
        DREAM: { variant: "purple", label: "DREAM" },
    };
    const cat = categoryMap[item.recommendationCategory] ?? { variant: "default" as const, label: item.recommendationCategory };
    const diff = Number(item.percentileDifference ?? 0);
    const diffSign = diff > 0 ? `+${diff.toFixed(2)}` : diff.toFixed(2);

    return (
        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false} glowColor={
            item.recommendationCategory === "SAFE" ? "rgba(57,255,20,0.04)" :
            item.recommendationCategory === "TARGET" ? "rgba(0,240,255,0.04)" :
            "rgba(138,43,226,0.04)"
        }>
            {/* Header */}
            <div className="flex justify-between items-start gap-3">
                <div className="flex flex-col gap-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        <Badge variant={cat.variant} glow className="text-[9px] px-2 py-0.5 shrink-0">{cat.label}</Badge>
                        <span className="font-bold text-sm text-white truncate">{item.collegeName}</span>
                    </div>
                    <span className="text-[11px] text-text-tertiary">
                        {item.branchName} · {item.city}, {item.state}
                    </span>
                </div>
                <div className="shrink-0 text-right">
                    <span className={`text-sm font-bold font-futuristic ${diff > 0 ? "text-accent-green" : diff < 0 ? "text-accent-orange" : "text-text-secondary"}`}>
                        {diffSign}%
                    </span>
                    <div className="text-[9px] text-text-tertiary mt-0.5">vs closing</div>
                </div>
            </div>

            {/* AI Explanation */}
            {item.humanReadableReason && (
                <div className="flex items-start gap-2.5 px-3 py-2.5 rounded-xs border border-border-color/30 bg-white/2 text-[11px] text-text-secondary">
                    <Brain className="w-3.5 h-3.5 shrink-0 mt-0.5 text-accent-purple" />
                    <span className="leading-relaxed">{item.humanReadableReason}</span>
                </div>
            )}

            {/* Stats Row */}
            <div className="grid grid-cols-3 gap-3 text-[11px]">
                <div className="flex flex-col gap-0.5">
                    <span className="text-text-tertiary">Closing %ile</span>
                    <span className="font-semibold text-white">{Number(item.closingPercentile).toFixed(2)}</span>
                </div>
                <div className="flex flex-col gap-0.5">
                    <span className="text-text-tertiary">Avg Package</span>
                    <span className="font-semibold text-accent-green">
                        {item.averagePackage ? `₹${(Number(item.averagePackage) / 100000).toFixed(1)}L` : "N/A"}
                    </span>
                </div>
                <div className="flex flex-col gap-0.5">
                    <span className="text-text-tertiary">Fees/Year</span>
                    <span className="font-semibold text-white">
                        {item.feesPerYear ? `₹${(Number(item.feesPerYear) / 1000).toFixed(0)}K` : "N/A"}
                    </span>
                </div>
            </div>

            {/* Actions */}
            <div className="flex gap-2 pt-1 border-t border-border-color/20">
                <Button variant="secondary" size="sm" className="flex-1 gap-1.5" onClick={() => onView(item.collegeId)}>
                    <ArrowRight className="w-3.5 h-3.5" /> View Profile
                </Button>
                <Button
                    variant="primary"
                    size="sm"
                    className="flex-1 gap-1.5"
                    isLoading={isShortlisting}
                    onClick={() => onShortlist(item.id)}
                >
                    <Star className="w-3.5 h-3.5" /> Shortlist
                </Button>
            </div>
        </Card>
    );
}

// ─── Summary Bar ──────────────────────────────────────────────────────────────

function SummaryBar({ result }: { result: RecommendationResult }) {
    const stats = [
        { label: "Evaluated", value: result.evaluatedCount, icon: <BookOpen className="w-3.5 h-3.5 text-text-secondary" /> },
        { label: "Safe Options", value: result.safeCount, icon: <CheckCircle className="w-3.5 h-3.5 text-accent-green" /> },
        { label: "Target Options", value: result.targetCount, icon: <AlertCircle className="w-3.5 h-3.5 text-accent-cyan" /> },
        { label: "Dream Options", value: result.dreamCount, icon: <Sparkles className="w-3.5 h-3.5 text-accent-purple" /> },
        { label: "Engine Time", value: `${result.executionTimeMs}ms`, icon: <Zap className="w-3.5 h-3.5 text-accent-orange" /> },
        { label: "Cache Hit", value: result.cacheHit ? "Yes" : "No", icon: <TrendingUp className="w-3.5 h-3.5 text-text-secondary" /> },
    ];

    return (
        <div className="grid grid-cols-3 md:grid-cols-6 gap-3">
            {stats.map((s, i) => (
                <Card key={i} className="flex flex-col gap-1.5 p-3" hoverLift={false}>
                    <div className="flex items-center justify-between">
                        <span className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">{s.label}</span>
                        {s.icon}
                    </div>
                    <span className="text-lg font-light font-futuristic text-white">{s.value}</span>
                </Card>
            ))}
        </div>
    );
}

// ─── Wizard ───────────────────────────────────────────────────────────────────

const TOTAL_STEPS = 6;

function Wizard({ onGenerate, isLoading }: {
    onGenerate: (payload: Parameters<ReturnType<typeof useGenerateRecommendation>["mutate"]>[0]) => void;
    isLoading: boolean;
}) {
    const [step, setStep] = useState(1);
    const [form, setForm] = useState({
        exam: "MHT_CET",
        year: "2024",
        percentile: "",
        rank: "",
        category: "OPEN",
        branches: [] as string[],
        cities: [] as string[],
        collegeType: "ALL",
        minimumNAAC: "",
        maximumFees: "",
    });

    const update = (fields: Partial<typeof form>) => setForm(p => ({ ...p, ...fields }));

    const toggleBranch = (b: string) =>
        update({ branches: form.branches.includes(b) ? form.branches.filter(x => x !== b) : [...form.branches, b] });
    const toggleCity = (c: string) =>
        update({ cities: form.cities.includes(c) ? form.cities.filter(x => x !== c) : [...form.cities, c] });

    const handleSubmit = () => {
        onGenerate({
            exam: form.exam,
            year: Number(form.year),
            percentile: Number(form.percentile),
            rank: form.rank ? Number(form.rank) : null,
            category: form.category,
            preferredBranches: form.branches.length > 0 ? form.branches : undefined,
            preferredCities: form.cities.length > 0 ? form.cities : undefined,
            preferredCollegeTypes: form.collegeType !== "ALL" ? [form.collegeType] : undefined,
            minimumNAAC: form.minimumNAAC || null,
            maximumFees: form.maximumFees ? Number(form.maximumFees) : null,
        });
    };

    const progress = (step / TOTAL_STEPS) * 100;

    return (
        <Card className="flex flex-col gap-6 p-6 text-left" hoverLift={false} glowColor="rgba(0,240,255,0.06)">
            {/* Progress */}
            <div className="flex flex-col gap-2">
                <div className="flex justify-between text-[10px] font-bold text-text-tertiary select-none uppercase tracking-wider">
                    <span>Configuration Wizard</span>
                    <span>Step {step} of {TOTAL_STEPS}</span>
                </div>
                <div className="h-0.5 w-full bg-border-color/30 rounded-full overflow-hidden">
                    <div className="h-full bg-gradient-to-r from-accent-cyan to-accent-purple rounded-full transition-all duration-500" style={{ width: `${progress}%` }} />
                </div>
            </div>

            {/* Steps */}
            <div className="min-h-[200px] flex flex-col justify-center gap-5">
                {step === 1 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Entrance Examination</h3>
                        <Dropdown
                            options={[{ label: "MHT-CET", value: "MHT_CET" }, { label: "JEE Main", value: "JEE_MAIN" }]}
                            selected={form.exam} onChange={v => update({ exam: v })}
                        />
                        <Input label="Admission Year" type="number" value={form.year} onChange={e => update({ year: e.target.value })} />
                    </div>
                )}
                {step === 2 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Scores & Rank</h3>
                        <Input label="Percentile Score *" type="number" placeholder="e.g. 98.42" value={form.percentile} onChange={e => update({ percentile: e.target.value })} required />
                        <Input label="State Merit Rank (optional)" type="number" placeholder="e.g. 4510" value={form.rank} onChange={e => update({ rank: e.target.value })} />
                    </div>
                )}
                {step === 3 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Category & Reservation</h3>
                        <Dropdown
                            options={["OPEN","OBC","SC","ST","EWS","NT1","NT2","NT3"].map(v => ({ label: v, value: v }))}
                            selected={form.category} onChange={v => update({ category: v })}
                        />
                    </div>
                )}
                {step === 4 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Preferred Branches</h3>
                        <div className="flex flex-wrap gap-2">
                            {["Computer Engineering","Information Technology","Electronics & Telecom","Electrical Engineering","Mechanical Engineering","Civil Engineering"].map(b => (
                                <button key={b} onClick={() => toggleBranch(b)}
                                    className={`glass-sm px-3 py-2 rounded-xs text-[11px] font-semibold border cursor-pointer transition-all select-none ${form.branches.includes(b) ? "border-accent-cyan bg-accent-cyan/10 text-white" : "border-border-color text-text-secondary hover:text-white"}`}>
                                    {b}
                                </button>
                            ))}
                        </div>
                    </div>
                )}
                {step === 5 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Preferred Cities & College Type</h3>
                        <div className="flex flex-wrap gap-2">
                            {["Pune","Mumbai","Nagpur","Nashik","Aurangabad","Sangli","Kolhapur"].map(c => (
                                <button key={c} onClick={() => toggleCity(c)}
                                    className={`glass-sm px-3 py-2 rounded-xs text-[11px] font-semibold border cursor-pointer transition-all select-none ${form.cities.includes(c) ? "border-accent-purple bg-accent-purple/10 text-white" : "border-border-color text-text-secondary hover:text-white"}`}>
                                    {c}
                                </button>
                            ))}
                        </div>
                        <Dropdown
                            options={[{ label: "All Types", value: "ALL" }, { label: "Government", value: "GOVERNMENT" }, { label: "Private", value: "PRIVATE" }]}
                            selected={form.collegeType} onChange={v => update({ collegeType: v })}
                        />
                    </div>
                )}
                {step === 6 && (
                    <div className="flex flex-col gap-4">
                        <h3 className="text-sm font-bold text-white">Advanced Filters</h3>
                        <Dropdown
                            options={[{ label: "Any NAAC Grade", value: "" }, { label: "A++ or above", value: "A++" }, { label: "A+ or above", value: "A+" }, { label: "A or above", value: "A" }]}
                            selected={form.minimumNAAC} onChange={v => update({ minimumNAAC: v })}
                        />
                        <Input label="Max Annual Fees (INR)" type="number" placeholder="e.g. 150000" value={form.maximumFees} onChange={e => update({ maximumFees: e.target.value })} />
                    </div>
                )}
            </div>

            {/* Navigation */}
            <div className="flex justify-between pt-2 border-t border-border-color/20">
                <Button variant="secondary" size="sm" disabled={step === 1 || isLoading} onClick={() => setStep(s => s - 1)}>
                    Back
                </Button>
                {step < TOTAL_STEPS ? (
                    <Button variant="primary" size="sm" disabled={step === 2 && !form.percentile} onClick={() => setStep(s => s + 1)}>
                        Next <ArrowRight className="w-3.5 h-3.5 ml-1" />
                    </Button>
                ) : (
                    <Button variant="primary" size="sm" isLoading={isLoading} onClick={handleSubmit}>
                        <Sparkles className="w-3.5 h-3.5 mr-1" /> Generate Matches
                    </Button>
                )}
            </div>
        </Card>
    );
}

// ─── History Item ─────────────────────────────────────────────────────────────

function HistoryItem({ item, onOpen }: { item: RecommendationHistory; onOpen: (id: string) => void }) {
    const date = new Date(item.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });
    return (
        <button onClick={() => onOpen(item.id)}
            className="w-full text-left flex justify-between items-center p-4 glass-sm border border-border-color/30 rounded-xs hover:border-accent-cyan/30 transition-all cursor-pointer group">
            <div className="flex flex-col gap-1">
                <div className="flex items-center gap-2">
                    <span className="text-[11px] font-bold text-white">{item.examName.replace("_", " ")} · {item.percentile}%ile</span>
                    <Badge variant="cyan" className="text-[8.5px] px-1.5 py-0">{item.returnedCount} results</Badge>
                    {item.cacheHit && <Badge variant="purple" className="text-[8.5px] px-1.5 py-0">Cached</Badge>}
                </div>
                <span className="text-[10px] text-text-tertiary">{item.category} · {date}</span>
            </div>
            <ArrowRight className="w-4 h-4 text-text-tertiary group-hover:text-accent-cyan transition-colors shrink-0" />
        </button>
    );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function RecommendationsPage() {
    const router = useRouter();
    const toast = useToast();
    const [activeTab, setActiveTab] = useState<"generate" | "history">("generate");
    const [result, setResult] = useState<RecommendationResult | null>(null);
    const [shortlistingId, setShortlistingId] = useState<string | null>(null);

    const { mutate: generate, isPending: isGenerating } = useGenerateRecommendation();
    const { data: history, isLoading: historyLoading } = useRecommendationHistory();
    const { mutate: shortlist } = useShortlistRecommendation();

    const handleGenerate = (payload: Parameters<typeof generate>[0]) => {
        generate(payload, {
            onSuccess: (data) => {
                setResult(data);
                toast.success(`Generated ${data.returnedCount} recommendations in ${data.executionTimeMs}ms!`);
            },
            onError: () => toast.error("Failed to generate recommendations. Check your scores and filters."),
        });
    };

    const handleShortlist = (itemId: string) => {
        setShortlistingId(itemId);
        shortlist(itemId, {
            onSuccess: () => { toast.success("Added to shortlist successfully!"); setShortlistingId(null); },
            onError: () => { toast.error("Failed to add to shortlist."); setShortlistingId(null); },
        });
    };

    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-64 pt-24 pb-16 px-6 relative overflow-hidden">
                <Navbar />
                <Sidebar activeItem="recommendations" onChange={() => {}} />

                {/* Ambient glow */}
                <div className="absolute inset-0 opacity-[0.02] pointer-events-none z-0">
                    <div className="absolute top-[15%] left-[25%] w-96 h-96 bg-accent-purple rounded-full blur-[150px]" />
                    <div className="absolute bottom-[15%] right-[25%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px]" />
                </div>

                <div className="max-w-5xl mx-auto flex flex-col gap-8 relative z-10">
                    {/* Page Header */}
                    <ScrollReveal>
                        <div className="flex flex-col gap-2 text-left select-none">
                            <Badge variant="purple" glow className="w-fit text-[9px] px-2.5 py-1">
                                <Brain className="w-3 h-3 mr-1.5 inline" /> AI Recommendation Engine
                            </Badge>
                            <h1 className="text-3xl md:text-5xl font-extrabold text-white tracking-tight">
                                Smart{" "}
                                <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-purple to-accent-cyan">
                                    College Matches
                                </span>
                            </h1>
                            <p className="text-xs text-text-secondary max-w-xl leading-relaxed mt-1">
                                Our AI engine evaluates thousands of historical cutoff records to generate ranked SAFE, TARGET, and DREAM college recommendations personalised to your scores.
                            </p>
                        </div>
                    </ScrollReveal>

                    {/* Tab Bar */}
                    <div className="flex gap-3 border-b border-border-color/20 pb-1 select-none">
                        {(["generate", "history"] as const).map(tab => (
                            <button key={tab} onClick={() => setActiveTab(tab)}
                                className={`text-xs font-bold pb-2 px-1 capitalize border-b-2 transition-all cursor-pointer ${activeTab === tab ? "border-accent-cyan text-white" : "border-transparent text-text-secondary hover:text-white"}`}>
                                {tab === "generate" ? "Generate Recommendations" : "Session History"}
                            </button>
                        ))}
                    </div>

                    {/* ── Generate Tab ── */}
                    {activeTab === "generate" && (
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
                            {/* Wizard */}
                            <div className="md:col-span-1">
                                <ScrollReveal>
                                    <Wizard onGenerate={handleGenerate} isLoading={isGenerating} />
                                </ScrollReveal>
                            </div>

                            {/* Results */}
                            <div className="md:col-span-2 flex flex-col gap-6">
                                {isGenerating && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col items-center gap-4 py-12 select-none">
                                            <div className="w-14 h-14 rounded-full border border-accent-purple/30 bg-accent-purple/5 flex items-center justify-center animate-pulse">
                                                <Brain className="w-6 h-6 text-accent-purple" />
                                            </div>
                                            <p className="text-sm text-text-secondary">Evaluating cutoff records across all institutions…</p>
                                        </div>
                                        {[1, 2, 3].map(x => <Skeleton key={x} className="h-36 w-full" />)}
                                    </div>
                                )}

                                {!isGenerating && result && (
                                    <div className="flex flex-col gap-6">
                                        <ScrollReveal>
                                            <SummaryBar result={result} />
                                        </ScrollReveal>

                                        {result.items.length > 0 ? (
                                            <div className="flex flex-col gap-4">
                                                {result.items.map((item, idx) => (
                                                    <ScrollReveal key={`${item.id}-${idx}`} delay={idx * 0.05}>
                                                        <RecommendationCard
                                                            item={item}
                                                            onShortlist={handleShortlist}
                                                            isShortlisting={shortlistingId === item.id}
                                                            onView={id => router.push(`/app/colleges/${id}`)}
                                                        />
                                                    </ScrollReveal>
                                                ))}
                                            </div>
                                        ) : (
                                            <EmptyState
                                                title="No matches found"
                                                description="No colleges match your current filters. Try broadening your category, city, or fees preferences."
                                                action={
                                                    <Button variant="primary" size="sm" onClick={() => setResult(null)}>
                                                        Adjust Filters <PlusCircle className="w-3.5 h-3.5 ml-1" />
                                                    </Button>
                                                }
                                            />
                                        )}
                                    </div>
                                )}

                                {!isGenerating && !result && (
                                    <div className="flex flex-col items-center justify-center py-24 gap-5 select-none text-center">
                                        <div className="w-16 h-16 rounded-full border border-accent-purple/20 bg-accent-purple/5 flex items-center justify-center">
                                            <Sparkles className="w-7 h-7 text-accent-purple animate-pulse" />
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <p className="text-sm font-bold text-white">Ready to find your matches</p>
                                            <p className="text-[11px] text-text-secondary max-w-xs leading-relaxed">Complete the wizard on the left and click Generate to run the AI engine.</p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* ── History Tab ── */}
                    {activeTab === "history" && (
                        <div className="flex flex-col gap-4">
                            {historyLoading ? (
                                <div className="flex flex-col gap-3">
                                    {[1, 2, 3, 4].map(x => <Skeleton key={x} className="h-16 w-full" />)}
                                </div>
                            ) : history && history.length > 0 ? (
                                history.map((item) => (
                                    <ScrollReveal key={item.id}>
                                        <HistoryItem item={item} onOpen={id => router.push(`/app/recommendations/${id}`)} />
                                    </ScrollReveal>
                                ))
                            ) : (
                                <EmptyState
                                    title="No recommendation history"
                                    description="You haven't generated any recommendations yet. Switch to the Generate tab to get started."
                                    action={
                                        <Button variant="primary" size="sm" onClick={() => setActiveTab("generate")}>
                                            Generate Now <Sparkles className="w-3.5 h-3.5 ml-1" />
                                        </Button>
                                    }
                                />
                            )}
                        </div>
                    )}
                </div>
            </div>
        </ProtectedRoute>
    );
}
