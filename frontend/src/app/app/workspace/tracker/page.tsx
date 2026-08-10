"use client";

import React, { useState } from "react";
import { WorkspaceLayout } from "@/app/app/workspace/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import {
    useShortlists,
    useTrackerHistory,
    useUpdateTracker,
    type ShortlistItem,
    type TrackerItem,
    type TrackerHistoryItem,
    type AdmissionStatus,
} from "@/hooks/useWorkflow";
import {
    MapPin,
    ChevronRight,
    Clock,
    CheckCircle,
    XCircle,
    Circle,
    Sparkles,
    ChevronDown,
    ArrowRight,
} from "lucide-react";
import { useRouter } from "next/navigation";

// ─── Status Configuration ─────────────────────────────────────────────────────

const STATUS_ORDER: AdmissionStatus[] = [
    "INTERESTED",
    "APPLIED",
    "DOCUMENTS_UPLOADED",
    "DOCUMENTS_VERIFIED",
    "SEAT_ALLOTTED",
    "CONFIRMED",
];

const TERMINAL_STATUSES: AdmissionStatus[] = ["REJECTED", "WITHDRAWN"];

const STATUS_META: Record<AdmissionStatus, { label: string; color: string; bg: string; icon: React.ReactNode }> = {
    INTERESTED:          { label: "Interested",         color: "text-text-secondary",  bg: "bg-white/5",             icon: <Circle className="w-3.5 h-3.5" /> },
    APPLIED:             { label: "Applied",            color: "text-accent-cyan",     bg: "bg-accent-cyan/10",      icon: <ChevronRight className="w-3.5 h-3.5" /> },
    DOCUMENTS_UPLOADED:  { label: "Docs Uploaded",      color: "text-accent-cyan",     bg: "bg-accent-cyan/10",      icon: <ChevronRight className="w-3.5 h-3.5" /> },
    DOCUMENTS_VERIFIED:  { label: "Docs Verified",      color: "text-accent-purple",   bg: "bg-accent-purple/10",    icon: <CheckCircle className="w-3.5 h-3.5" /> },
    SEAT_ALLOTTED:       { label: "Seat Allotted",      color: "text-accent-green",    bg: "bg-accent-green/10",     icon: <CheckCircle className="w-3.5 h-3.5" /> },
    CONFIRMED:           { label: "Confirmed ✓",        color: "text-accent-green",    bg: "bg-accent-green/15",     icon: <CheckCircle className="w-3.5 h-3.5" /> },
    REJECTED:            { label: "Rejected",           color: "text-accent-orange",   bg: "bg-accent-orange/10",    icon: <XCircle className="w-3.5 h-3.5" /> },
    WITHDRAWN:           { label: "Withdrawn",          color: "text-text-tertiary",   bg: "bg-white/3",             icon: <XCircle className="w-3.5 h-3.5" /> },
};

// ─── Progress ─────────────────────────────────────────────────────────────────

function getProgress(status: AdmissionStatus): number {
    const idx = STATUS_ORDER.indexOf(status);
    if (idx === -1) return 0;
    return Math.round(((idx + 1) / STATUS_ORDER.length) * 100);
}

// ─── Timeline Dot ─────────────────────────────────────────────────────────────

function TimelineDot({ done, active }: { done: boolean; active: boolean }) {
    return (
        <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-all
            ${active ? "border-accent-cyan bg-accent-cyan/20 scale-110" : done ? "border-accent-green bg-accent-green/15" : "border-border-color bg-white/3"}`}>
            {done && !active && <div className="w-2 h-2 rounded-full bg-accent-green" />}
            {active && <div className="w-2 h-2 rounded-full bg-accent-cyan animate-pulse" />}
        </div>
    );
}

// ─── History Feed ─────────────────────────────────────────────────────────────

function HistoryFeed({ history }: { history: TrackerHistoryItem[] }) {
    return (
        <div className="flex flex-col gap-0">
            {history.map((h, i) => {
                const fromMeta = STATUS_META[h.previousStatus];
                const toMeta = STATUS_META[h.newStatus];
                const date = new Date(h.changedAt).toLocaleString("en-IN");
                return (
                    <div key={h.id} className="flex gap-3 relative pb-5 last:pb-0">
                        {i < history.length - 1 && (
                            <div className="absolute left-[9px] top-5 bottom-0 w-px bg-border-color/30" />
                        )}
                        <div className="w-[18px] h-[18px] rounded-full border border-border-color/40 bg-white/5 flex items-center justify-center shrink-0 mt-0.5">
                            <div className="w-1.5 h-1.5 rounded-full bg-accent-cyan" />
                        </div>
                        <div className="flex flex-col gap-0.5 min-w-0">
                            <div className="flex items-center gap-2 text-[11px]">
                                <span className={`font-semibold ${fromMeta.color}`}>{fromMeta.label}</span>
                                <ArrowRight className="w-3 h-3 text-text-tertiary shrink-0" />
                                <span className={`font-semibold ${toMeta.color}`}>{toMeta.label}</span>
                            </div>
                            {h.remarks && <p className="text-[10px] text-text-secondary">{h.remarks}</p>}
                            <span className="text-[9px] text-text-tertiary flex items-center gap-1 mt-0.5"><Clock className="w-2.5 h-2.5" /> {date}</span>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}

// ─── Tracker Card ─────────────────────────────────────────────────────────────

function TrackerCard({ shortlist, tracker }: { shortlist: ShortlistItem; tracker: TrackerItem | null }) {
    const toast = useToast();
    const [expanded, setExpanded] = useState(false);
    const [updating, setUpdating] = useState(false);
    const [selectedStatus, setSelectedStatus] = useState<AdmissionStatus | "">("");
    const [remarks, setRemarks] = useState("");

    const { data: history, isLoading: historyLoading } = useTrackerHistory(expanded && tracker ? tracker.id : undefined);
    const { mutate: updateTracker } = useUpdateTracker();

    const currentStatus = tracker?.currentStatus as AdmissionStatus | undefined;
    const currentMeta = currentStatus ? STATUS_META[currentStatus] : STATUS_META["INTERESTED"];
    const progress = currentStatus ? getProgress(currentStatus) : 0;
    const isTerminal = currentStatus ? TERMINAL_STATUSES.includes(currentStatus) : false;

    const handleUpdate = () => {
        if (!tracker || !selectedStatus) return;
        setUpdating(true);
        updateTracker({ id: tracker.id, status: selectedStatus, remarks: remarks || undefined }, {
            onSuccess: () => { toast.success("Status updated!"); setUpdating(false); setSelectedStatus(""); setRemarks(""); },
            onError: () => { toast.error("Failed to update status."); setUpdating(false); },
        });
    };

    const nextStatuses = currentStatus
        ? [...STATUS_ORDER.slice(STATUS_ORDER.indexOf(currentStatus) + 1), ...TERMINAL_STATUSES]
        : [];

    return (
        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
            {/* Header */}
            <div className="flex items-start justify-between gap-3">
                <div className="flex flex-col gap-1 min-w-0">
                    <h3 className="font-bold text-sm text-white">{shortlist.collegeName}</h3>
                    <p className="text-[11px] text-text-tertiary">{shortlist.branchName}</p>
                    <div className="flex items-center gap-1.5 text-[10px] text-text-tertiary mt-0.5">
                        <MapPin className="w-3 h-3" /> {shortlist.city}
                    </div>
                </div>
                <div className={`px-2.5 py-1 rounded-xs border text-[9px] font-bold flex items-center gap-1 shrink-0 ${currentMeta.color} border-current/20 ${currentMeta.bg}`}>
                    {currentMeta.icon} {currentMeta.label}
                </div>
            </div>

            {/* Progress Bar */}
            {!isTerminal && (
                <div className="flex flex-col gap-1.5">
                    <div className="flex justify-between text-[9px] text-text-tertiary">
                        <span>Application Progress</span>
                        <span>{progress}%</span>
                    </div>
                    <div className="h-1 w-full bg-border-color/20 rounded-full overflow-hidden">
                        <div className="h-full bg-gradient-to-r from-accent-cyan to-accent-green rounded-full transition-all duration-700" style={{ width: `${progress}%` }} />
                    </div>
                </div>
            )}

            {/* Visual Timeline */}
            <div className="flex items-center gap-1 overflow-x-auto pb-1 scrollbar-hide">
                {STATUS_ORDER.map((s, i) => {
                    const statusIdx = currentStatus ? STATUS_ORDER.indexOf(currentStatus) : -1;
                    const done = statusIdx > i;
                    const active = statusIdx === i;
                    return (
                        <React.Fragment key={s}>
                            <TimelineDot done={done} active={active} />
                            {i < STATUS_ORDER.length - 1 && (
                                <div className={`flex-1 h-px min-w-[12px] transition-colors ${done ? "bg-accent-green/40" : "bg-border-color/20"}`} />
                            )}
                        </React.Fragment>
                    );
                })}
            </div>
            <div className="flex justify-between text-[8.5px] text-text-tertiary select-none">
                <span>Interested</span><span>Confirmed</span>
            </div>

            {/* Update Status */}
            {!isTerminal && nextStatuses.length > 0 && (
                <div className="flex gap-2 flex-wrap">
                    <select value={selectedStatus} onChange={e => setSelectedStatus(e.target.value as AdmissionStatus)}
                        className="flex-1 min-w-0 glass-sm border border-border-color/40 rounded-xs px-2.5 py-2 text-[11px] text-white bg-transparent focus:outline-none focus:border-accent-cyan/50 cursor-pointer">
                        <option value="" className="bg-black">Select next status…</option>
                        {nextStatuses.map(s => (
                            <option key={s} value={s} className="bg-black">{STATUS_META[s].label}</option>
                        ))}
                    </select>
                    {selectedStatus && (
                        <>
                            <input value={remarks} onChange={e => setRemarks(e.target.value)} placeholder="Remarks (optional)"
                                className="flex-1 min-w-0 glass-sm border border-border-color/40 rounded-xs px-2.5 py-2 text-[11px] text-white bg-transparent focus:outline-none focus:border-accent-cyan/50 placeholder:text-text-tertiary" />
                            <Button variant="primary" size="sm" isLoading={updating} onClick={handleUpdate}>Update</Button>
                        </>
                    )}
                </div>
            )}

            {/* Timeline History Toggle */}
            {tracker && (
                <button onClick={() => setExpanded(p => !p)}
                    className="flex items-center gap-2 text-[10px] font-semibold text-text-secondary hover:text-white transition-colors cursor-pointer select-none pt-1 border-t border-border-color/20">
                    <ChevronDown className={`w-3.5 h-3.5 transition-transform ${expanded ? "rotate-180" : ""}`} />
                    {expanded ? "Hide History" : "View Timeline History"}
                </button>
            )}

            {expanded && (
                <div className="pl-2 border-l border-border-color/30">
                    {historyLoading ? (
                        <div className="flex flex-col gap-2">{[1, 2].map(x => <Skeleton key={x} className="h-8 w-full" />)}</div>
                    ) : history && history.length > 0 ? (
                        <HistoryFeed history={history} />
                    ) : (
                        <p className="text-[11px] text-text-tertiary">No history yet.</p>
                    )}
                </div>
            )}
        </Card>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function TrackerPage() {
    const router = useRouter();
    const { data: shortlists, isLoading } = useShortlists();
    const active = (shortlists ?? []).filter(s => !s.isDeleted);

    return (
        <WorkspaceLayout>
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="cyan" glow className="w-fit text-[9px] px-2.5 py-1">
                            <MapPin className="w-3 h-3 mr-1.5 inline" /> Admission Tracker
                        </Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight">
                            Application{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan to-accent-green">
                                Journey
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary max-w-md leading-relaxed">
                            Visual timeline of your admission progress across all shortlisted colleges. Update status and track every step.
                        </p>
                    </div>
                </ScrollReveal>

                {isLoading ? (
                    <div className="flex flex-col gap-4">
                        {[1, 2, 3].map(x => <Skeleton key={x} className="h-64 w-full" />)}
                    </div>
                ) : active.length > 0 ? (
                    <div className="flex flex-col gap-5">
                        {active.map((s, i) => (
                            <ScrollReveal key={s.id} delay={i * 0.06}>
                                <TrackerCard shortlist={s} tracker={s.tracker ?? null} />
                            </ScrollReveal>
                        ))}
                    </div>
                ) : (
                    <EmptyState
                        title="No applications to track"
                        description="Shortlist college branches from your recommendations to start tracking your admission journey."
                        action={
                            <Button variant="primary" size="sm" onClick={() => router.push("/app/recommendations")}>
                                <Sparkles className="w-3.5 h-3.5 mr-1.5" /> Get Recommendations
                            </Button>
                        }
                    />
                )}
            </div>
        </WorkspaceLayout>
    );
}
