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
    useUpdateShortlist,
    useDeleteShortlist,
    type ShortlistItem,
    type AdmissionStatus,
} from "@/hooks/useWorkflow";
import {
    Star,
    Trash2,
    ArrowRight,
    MapPin,
    Pencil,
    Check,
    X,
    Sparkles,
} from "lucide-react";
import { useRouter } from "next/navigation";

// ─── Status Map ───────────────────────────────────────────────────────────────

const STATUS_META: Record<AdmissionStatus, { label: string; variant: "green" | "cyan" | "purple" | "orange" | "default" }> = {
    INTERESTED:          { label: "Interested",          variant: "default" },
    APPLIED:             { label: "Applied",             variant: "cyan" },
    DOCUMENTS_UPLOADED:  { label: "Docs Uploaded",       variant: "cyan" },
    DOCUMENTS_VERIFIED:  { label: "Docs Verified",       variant: "purple" },
    SEAT_ALLOTTED:       { label: "Seat Allotted",       variant: "green" },
    CONFIRMED:           { label: "Confirmed",           variant: "green" },
    REJECTED:            { label: "Rejected",            variant: "orange" },
    WITHDRAWN:           { label: "Withdrawn",           variant: "default" },
};

// ─── Priority Badge ───────────────────────────────────────────────────────────

function PriorityBadge({ priority }: { priority: number }) {
    const colors = ["bg-accent-green/15 text-accent-green border-accent-green/30", "bg-accent-cyan/15 text-accent-cyan border-accent-cyan/30", "bg-accent-purple/15 text-accent-purple border-accent-purple/30"];
    const color = colors[Math.min(priority - 1, 2)] ?? "bg-white/5 text-text-secondary border-border-color";
    return (
        <span className={`text-[9px] font-bold px-2 py-0.5 rounded-xs border select-none ${color}`}>
            P{priority}
        </span>
    );
}

// ─── Shortlist Card ───────────────────────────────────────────────────────────

function ShortlistCard({
    item,
    onDelete,
    onUpdate,
    onView,
    isDeleting,
}: {
    item: ShortlistItem;
    onDelete: (id: string) => void;
    onUpdate: (id: string, priority: number, notes: string) => void;
    onView: (id: string) => void;
    isDeleting: boolean;
}) {
    const [editing, setEditing] = useState(false);
    const [priority, setPriority] = useState(String(item.priority ?? 1));
    const [notes, setNotes] = useState(item.notes ?? "");

    const trackerStatus = item.tracker?.currentStatus as AdmissionStatus | undefined;
    const statusMeta = trackerStatus ? STATUS_META[trackerStatus] : null;

    const handleSave = () => {
        onUpdate(item.id, Number(priority) || 1, notes);
        setEditing(false);
    };

    return (
        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={!editing}>
            {/* Header */}
            <div className="flex items-start justify-between gap-3">
                <div className="flex flex-col gap-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        <PriorityBadge priority={item.priority ?? 1} />
                        {statusMeta && <Badge variant={statusMeta.variant} className="text-[8.5px] px-1.5 py-0">{statusMeta.label}</Badge>}
                        {item.naacGrade && <Badge variant="cyan" className="text-[8.5px] px-1.5 py-0">{item.naacGrade}</Badge>}
                    </div>
                    <h3 className="font-bold text-sm text-white mt-1">{item.collegeName}</h3>
                    <p className="text-[11px] text-text-tertiary">{item.branchName}</p>
                    <div className="flex items-center gap-1.5 text-[10px] text-text-tertiary mt-0.5">
                        <MapPin className="w-3 h-3" /> {item.city}, {item.state}
                    </div>
                </div>
                <div className="w-9 h-9 rounded-xs border border-border-color/20 bg-accent-yellow/5 flex items-center justify-center shrink-0">
                    <Star className="w-4 h-4 text-accent-yellow" />
                </div>
            </div>

            {/* Edit / Notes */}
            {editing ? (
                <div className="flex flex-col gap-2.5">
                    <div className="flex gap-2">
                        <div className="flex flex-col gap-1 flex-1">
                            <label className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">Priority</label>
                            <input type="number" min={1} max={99} value={priority} onChange={e => setPriority(e.target.value)}
                                className="glass-sm border border-border-color/40 rounded-xs px-2.5 py-2 text-[11px] text-white bg-transparent focus:outline-none focus:border-accent-cyan/50 w-full" />
                        </div>
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">Notes</label>
                        <textarea rows={2} value={notes} onChange={e => setNotes(e.target.value)} placeholder="Add personal notes…"
                            className="glass-sm border border-border-color/40 rounded-xs px-2.5 py-2 text-[11px] text-white bg-transparent focus:outline-none focus:border-accent-cyan/50 w-full resize-none placeholder:text-text-tertiary" />
                    </div>
                    <div className="flex gap-2">
                        <Button variant="primary" size="sm" className="flex-1 gap-1.5" onClick={handleSave}><Check className="w-3.5 h-3.5" /> Save</Button>
                        <Button variant="secondary" size="sm" className="flex-1 gap-1.5" onClick={() => setEditing(false)}><X className="w-3.5 h-3.5" /> Cancel</Button>
                    </div>
                </div>
            ) : (
                <>
                    {item.notes && (
                        <p className="text-[11px] text-text-secondary px-3 py-2 glass-sm border border-border-color/20 rounded-xs leading-relaxed">{item.notes}</p>
                    )}
                    {item.feesPerYear && (
                        <p className="text-[10px] text-text-tertiary">Fees: ₹{(Number(item.feesPerYear) / 1000).toFixed(0)}K / yr</p>
                    )}
                </>
            )}

            {/* Actions */}
            {!editing && (
                <div className="flex gap-2 pt-1 border-t border-border-color/20">
                    <Button variant="secondary" size="sm" className="flex-1 gap-1.5" onClick={() => onView(item.collegeId)}>
                        <ArrowRight className="w-3.5 h-3.5" /> View
                    </Button>
                    <Button variant="secondary" size="sm" className="gap-1.5" onClick={() => { setPriority(String(item.priority ?? 1)); setNotes(item.notes ?? ""); setEditing(true); }}>
                        <Pencil className="w-3.5 h-3.5" />
                    </Button>
                    <Button variant="secondary" size="sm" className="gap-1.5 text-accent-orange hover:border-accent-orange/40" isLoading={isDeleting} onClick={() => onDelete(item.id)}>
                        <Trash2 className="w-3.5 h-3.5" />
                    </Button>
                </div>
            )}
        </Card>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function ShortlistsPage() {
    const router = useRouter();
    const toast = useToast();
    const [deletingId, setDeletingId] = useState<string | null>(null);

    const { data: shortlists, isLoading } = useShortlists();
    const { mutate: updateShortlist } = useUpdateShortlist();
    const { mutate: deleteShortlist } = useDeleteShortlist();

    const handleDelete = (id: string) => {
        setDeletingId(id);
        deleteShortlist(id, {
            onSuccess: () => { toast.success("Removed from shortlist."); setDeletingId(null); },
            onError: () => { toast.error("Failed to remove."); setDeletingId(null); },
        });
    };

    const handleUpdate = (id: string, priority: number, notes: string) => {
        updateShortlist({ id, priority, notes }, {
            onSuccess: () => toast.success("Shortlist updated!"),
            onError: () => toast.error("Failed to update shortlist."),
        });
    };

    return (
        <WorkspaceLayout activeItem="workspace">
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="orange" glow className="w-fit text-[9px] px-2.5 py-1">
                            <Star className="w-3 h-3 mr-1.5 inline" /> Shortlists
                        </Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight">
                            Priority{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-orange to-accent-cyan">
                                Choices
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary max-w-md leading-relaxed">
                            Manage your shortlisted college branches with priority rankings, personal notes, and admission status at a glance.
                        </p>
                    </div>
                </ScrollReveal>

                {isLoading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                        {[1, 2, 3, 4, 5, 6].map(x => <Skeleton key={x} className="h-52" />)}
                    </div>
                ) : shortlists && shortlists.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                        {shortlists
                            .filter(s => !s.isDeleted)
                            .sort((a, b) => (a.priority ?? 99) - (b.priority ?? 99))
                            .map((item, i) => (
                                <ScrollReveal key={item.id} delay={i * 0.05}>
                                    <ShortlistCard
                                        item={item}
                                        onDelete={handleDelete}
                                        onUpdate={handleUpdate}
                                        onView={id => router.push(`/app/colleges/${id}`)}
                                        isDeleting={deletingId === item.id}
                                    />
                                </ScrollReveal>
                            ))}
                    </div>
                ) : (
                    <EmptyState
                        title="No shortlisted colleges"
                        description="Generate AI recommendations and shortlist your best matches to manage them here."
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
