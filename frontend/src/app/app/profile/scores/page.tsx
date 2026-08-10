"use client";

import React, { useState } from "react";
import { ProfileLayout } from "@/app/app/profile/page";
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
    useScores, useCreateScore, useUpdateScore, useDeleteScore,
    type ExamScore, type ExamScorePayload, type ExamName,
} from "@/hooks/useProfile";
import { GraduationCap, Plus, Pencil, Trash2, Check, X, Clock } from "lucide-react";

const EXAM_OPTIONS: { label: string; value: ExamName }[] = [
    { label: "MHT-CET", value: "MHT_CET" },
    { label: "JEE Main", value: "JEE_MAIN" },
    { label: "JEE Advanced", value: "JEE_ADVANCED" },
];

const EXAM_COLORS: Record<ExamName, string> = {
    MHT_CET:      "text-accent-cyan border-accent-cyan/20 bg-accent-cyan/8",
    JEE_MAIN:     "text-accent-purple border-accent-purple/20 bg-accent-purple/8",
    JEE_ADVANCED: "text-accent-orange border-accent-orange/20 bg-accent-orange/8",
};

// ─── Score Card ───────────────────────────────────────────────────────────────

function ScoreCard({ score, onDelete, onEdit, isDeleting }: {
    score: ExamScore;
    onDelete: (id: string) => void;
    onEdit: (score: ExamScore) => void;
    isDeleting: boolean;
}) {
    const colorCls = EXAM_COLORS[score.examName as ExamName] ?? "text-text-secondary border-border-color";
    const date = new Date(score.updatedAt).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });
    return (
        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
            <div className="flex items-start justify-between gap-3">
                <div className="flex flex-col gap-1">
                    <div className={`text-[10px] font-bold px-2.5 py-1 rounded-xs border w-fit select-none ${colorCls}`}>
                        {score.examName.replace("_", " ")} · {score.examYear}
                    </div>
                    <span className="text-2xl font-bold font-futuristic text-white mt-2">{Number(score.percentile).toFixed(2)}%ile</span>
                </div>
                <div className="flex gap-1.5">
                    <button onClick={() => onEdit(score)} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-cyan/40 transition-colors cursor-pointer">
                        <Pencil className="w-3.5 h-3.5 text-text-secondary" />
                    </button>
                    <button onClick={() => onDelete(score.id)} disabled={isDeleting} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-orange/40 transition-colors cursor-pointer disabled:opacity-50">
                        <Trash2 className="w-3.5 h-3.5 text-accent-orange" />
                    </button>
                </div>
            </div>
            <div className="grid grid-cols-3 gap-3 text-[11px]">
                <div><span className="text-text-tertiary block">Rank</span><span className="font-semibold text-white">{score.rank ?? "—"}</span></div>
                <div><span className="text-text-tertiary block">Marks</span><span className="font-semibold text-white">{score.marks ?? "—"}</span></div>
                <div><span className="text-text-tertiary block">Updated</span><span className="font-semibold text-text-secondary">{date}</span></div>
            </div>
            <div className="text-[9px] text-text-tertiary flex items-center gap-1 pt-1 border-t border-border-color/20">
                <Clock className="w-2.5 h-2.5" /> Score ID: {score.id.slice(0, 8)}…
            </div>
        </Card>
    );
}

// ─── Score Form ───────────────────────────────────────────────────────────────

function ScoreForm({ initial, onSave, onCancel, isPending }: {
    initial?: ExamScore;
    onSave: (payload: ExamScorePayload) => void;
    onCancel: () => void;
    isPending: boolean;
}) {
    const [form, setForm] = useState<ExamScorePayload>({
        examName: initial?.examName ?? "MHT_CET",
        examYear: initial?.examYear ?? new Date().getFullYear(),
        rank:       initial?.rank ?? 0,
        percentile: initial?.percentile ?? 0,
        marks:      initial?.marks ?? null,
    });

    const update = (fields: Partial<ExamScorePayload>) => setForm(p => ({ ...p, ...fields }));

    return (
        <Card className="flex flex-col gap-5 p-6 text-left border border-accent-cyan/20" hoverLift={false} glowColor="rgba(0,240,255,0.04)">
            <span className="text-[10px] font-bold uppercase tracking-wider text-accent-cyan">{initial ? "Edit Score" : "Add New Score"}</span>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Dropdown
                    options={EXAM_OPTIONS.map(e => ({ label: e.label, value: e.value }))}
                    selected={form.examName}
                    onChange={v => update({ examName: v as ExamName })}
                />
                <Input label="Exam Year *" type="number" value={String(form.examYear)} onChange={e => update({ examYear: Number(e.target.value) })} />
                <Input label="Percentile *" type="number" step="0.01" value={String(form.percentile)} onChange={e => update({ percentile: Number(e.target.value) })} />
                <Input label="Rank *" type="number" value={String(form.rank)} onChange={e => update({ rank: Number(e.target.value) })} />
                <Input label="Marks (optional)" type="number" value={form.marks != null ? String(form.marks) : ""} onChange={e => update({ marks: e.target.value ? Number(e.target.value) : null })} />
            </div>
            <div className="flex gap-2">
                <Button variant="primary" size="sm" isLoading={isPending} onClick={() => onSave(form)} className="gap-1.5">
                    <Check className="w-3.5 h-3.5" /> {initial ? "Update" : "Add Score"}
                </Button>
                <Button variant="secondary" size="sm" onClick={onCancel} className="gap-1.5">
                    <X className="w-3.5 h-3.5" /> Cancel
                </Button>
            </div>
        </Card>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function ExamScoresPage() {
    const toast = useToast();
    const [showForm, setShowForm] = useState(false);
    const [editingScore, setEditingScore] = useState<ExamScore | null>(null);
    const [deletingId, setDeletingId] = useState<string | null>(null);

    const { data: scores, isLoading } = useScores();
    const { mutate: createScore, isPending: createPending } = useCreateScore();
    const { mutate: updateScore, isPending: updatePending } = useUpdateScore();
    const { mutate: deleteScore } = useDeleteScore();

    const handleCreate = (payload: ExamScorePayload) => {
        createScore(payload, {
            onSuccess: () => { toast.success("Score added!"); setShowForm(false); },
            onError: (err: unknown) => {
                const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
                toast.error(msg ?? "Failed to add score.");
            },
        });
    };

    const handleUpdate = (payload: ExamScorePayload) => {
        if (!editingScore) return;
        updateScore({ id: editingScore.id, payload }, {
            onSuccess: () => { toast.success("Score updated!"); setEditingScore(null); },
            onError: (err: unknown) => {
                const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
                toast.error(msg ?? "Failed to update score.");
            },
        });
    };

    const handleDelete = (id: string) => {
        setDeletingId(id);
        deleteScore(id, {
            onSuccess: () => { toast.success("Score deleted."); setDeletingId(null); },
            onError: () => { toast.error("Failed to delete."); setDeletingId(null); },
        });
    };

    return (
        <ProfileLayout>
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex items-center justify-between gap-4">
                        <div className="flex flex-col gap-2">
                            <Badge variant="purple" className="w-fit text-[9px] px-2.5 py-1">
                                <GraduationCap className="w-3 h-3 mr-1.5 inline" /> Exam Scores
                            </Badge>
                            <h1 className="text-2xl md:text-3xl font-extrabold text-white">Entrance Exam Scores</h1>
                            <p className="text-xs text-text-secondary max-w-md">Manage your MHT-CET, JEE Main, and JEE Advanced scores used for AI recommendations.</p>
                        </div>
                        {!showForm && !editingScore && (
                            <Button variant="primary" size="sm" onClick={() => setShowForm(true)} className="gap-1.5 shrink-0">
                                <Plus className="w-3.5 h-3.5" /> Add Score
                            </Button>
                        )}
                    </div>
                </ScrollReveal>

                {showForm && (
                    <ScrollReveal>
                        <ScoreForm onSave={handleCreate} onCancel={() => setShowForm(false)} isPending={createPending} />
                    </ScrollReveal>
                )}

                {isLoading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {[1, 2, 3].map(x => <Skeleton key={x} className="h-40" />)}
                    </div>
                ) : scores && scores.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {scores.map((score, i) => (
                            <ScrollReveal key={score.id} delay={i * 0.05}>
                                {editingScore?.id === score.id ? (
                                    <ScoreForm initial={score} onSave={handleUpdate} onCancel={() => setEditingScore(null)} isPending={updatePending} />
                                ) : (
                                    <ScoreCard score={score} onDelete={handleDelete} onEdit={setEditingScore} isDeleting={deletingId === score.id} />
                                )}
                            </ScrollReveal>
                        ))}
                    </div>
                ) : !showForm ? (
                    <EmptyState
                        title="No exam scores yet"
                        description="Add your MHT-CET or JEE scores so the AI recommendation engine can find the best college matches for you."
                        action={<Button variant="primary" size="sm" onClick={() => setShowForm(true)}><Plus className="w-3.5 h-3.5 mr-1.5" /> Add First Score</Button>}
                    />
                ) : null}
            </div>
        </ProfileLayout>
    );
}
