"use client";

import React, { useState } from "react";
import { WorkspaceLayout } from "@/app/app/workspace/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import { useExportPdf, useExportCsv } from "@/hooks/useWorkflow";
import {
    Download,
    FileText,
    Sheet,
    Sparkles,
    Star,
    Heart,
    LayoutDashboard,
    CheckCircle,
} from "lucide-react";

// ─── Types ────────────────────────────────────────────────────────────────────

type ExportType = "dashboard" | "wishlist" | "shortlist" | "recommendations";

interface ExportOption {
    key: ExportType;
    label: string;
    desc: string;
    icon: React.ReactNode;
    variant: "default" | "green" | "cyan" | "purple" | "orange";
}

const EXPORTS: ExportOption[] = [
    { key: "dashboard", label: "Dashboard Report",       desc: "Complete snapshot of your admission progress, stats, and activity.", icon: <LayoutDashboard className="w-5 h-5" />, variant: "cyan" },
    { key: "wishlist",  label: "Wishlist Export",        desc: "All saved colleges with location, NAAC grade, and date added.",       icon: <Heart className="w-5 h-5" />,          variant: "orange" },
    { key: "shortlist", label: "Shortlist Export",       desc: "Priority-ranked college branches with notes, fees, and status.",       icon: <Star className="w-5 h-5" />,           variant: "purple" },
    { key: "recommendations", label: "Recommendation Summary", desc: "Latest AI recommendations with SAFE / TARGET / DREAM breakdown.", icon: <Sparkles className="w-5 h-5" />,      variant: "green" },
];

// ─── Export Card ──────────────────────────────────────────────────────────────

function ExportCard({
    option,
    onPdf,
    onCsv,
    isPdfLoading,
    isCsvLoading,
    lastExported,
}: {
    option: ExportOption;
    onPdf: (key: ExportType) => void;
    onCsv: (key: ExportType) => void;
    isPdfLoading: boolean;
    isCsvLoading: boolean;
    lastExported: "pdf" | "csv" | null;
}) {
    const colorMap: Record<string, string> = {
        cyan:   "text-accent-cyan",
        orange: "text-accent-orange",
        purple: "text-accent-purple",
        green:  "text-accent-green",
    };
    const iconColor = colorMap[option.variant] ?? "text-text-secondary";

    return (
        <Card className="flex flex-col gap-5 p-6 text-left" hoverLift={false}>
            <div className="flex items-start justify-between gap-3">
                <div className={`w-11 h-11 rounded-xs border border-border-color/20 bg-white/3 flex items-center justify-center shrink-0 ${iconColor}`}>
                    {option.icon}
                </div>
                {lastExported && (
                    <div className="flex items-center gap-1.5 text-[9px] font-bold text-accent-green select-none">
                        <CheckCircle className="w-3 h-3" /> Downloaded
                    </div>
                )}
            </div>
            <div className="flex flex-col gap-1.5">
                <h3 className="font-bold text-sm text-white">{option.label}</h3>
                <p className="text-[11px] text-text-secondary leading-relaxed">{option.desc}</p>
            </div>
            <div className="flex gap-2.5 pt-1 border-t border-border-color/20">
                <Button
                    variant="primary"
                    size="sm"
                    className="flex-1 gap-1.5"
                    isLoading={isPdfLoading}
                    disabled={isCsvLoading}
                    onClick={() => onPdf(option.key)}
                >
                    <FileText className="w-3.5 h-3.5" /> PDF
                </Button>
                <Button
                    variant="secondary"
                    size="sm"
                    className="flex-1 gap-1.5"
                    isLoading={isCsvLoading}
                    disabled={isPdfLoading}
                    onClick={() => onCsv(option.key)}
                >
                    <Sheet className="w-3.5 h-3.5" /> CSV
                </Button>
            </div>
        </Card>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function ExportPage() {
    const toast = useToast();
    const [pdfLoading, setPdfLoading] = useState<ExportType | null>(null);
    const [csvLoading, setCsvLoading] = useState<ExportType | null>(null);
    const [lastExported, setLastExported] = useState<Record<ExportType, "pdf" | "csv" | null>>({
        dashboard: null, wishlist: null, shortlist: null, recommendations: null,
    });

    const { mutate: exportPdf } = useExportPdf();
    const { mutate: exportCsv } = useExportCsv();

    const handlePdf = (key: ExportType) => {
        setPdfLoading(key);
        exportPdf(key, {
            onSuccess: () => {
                toast.success(`${key} PDF downloaded!`);
                setLastExported(p => ({ ...p, [key]: "pdf" }));
                setPdfLoading(null);
            },
            onError: () => {
                toast.error("Export failed. Please try again.");
                setPdfLoading(null);
            },
        });
    };

    const handleCsv = (key: ExportType) => {
        setCsvLoading(key);
        exportCsv(key, {
            onSuccess: () => {
                toast.success(`${key} CSV downloaded!`);
                setLastExported(p => ({ ...p, [key]: "csv" }));
                setCsvLoading(null);
            },
            onError: () => {
                toast.error("Export failed. Please try again.");
                setCsvLoading(null);
            },
        });
    };

    return (
        <WorkspaceLayout>
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="green" glow className="w-fit text-[9px] px-2.5 py-1">
                            <Download className="w-3 h-3 mr-1.5 inline" /> Export Center
                        </Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight">
                            Download{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-green to-accent-cyan">
                                Your Data
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary max-w-md leading-relaxed">
                            Export any section of your CampusSeekers data as PDF or CSV for offline use, counsellor sharing, or personal records.
                        </p>
                    </div>
                </ScrollReveal>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    {EXPORTS.map((opt, i) => (
                        <ScrollReveal key={opt.key} delay={i * 0.07}>
                            <ExportCard
                                option={opt}
                                onPdf={handlePdf}
                                onCsv={handleCsv}
                                isPdfLoading={pdfLoading === opt.key}
                                isCsvLoading={csvLoading === opt.key}
                                lastExported={lastExported[opt.key]}
                            />
                        </ScrollReveal>
                    ))}
                </div>
            </div>
        </WorkspaceLayout>
    );
}
