"use client";

import React, { useState } from "react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { FileDown, FileSpreadsheet } from "lucide-react";
import { api } from "@/lib/axios";
import { useToast } from "@/providers/ToastProvider";

export function DashboardExport() {
    const toast = useToast();
    const [loadingPdf, setLoadingPdf] = useState(false);
    const [loadingCsv, setLoadingCsv] = useState(false);

    const handleExport = async (format: "pdf" | "csv", type: string) => {
        if (format === "pdf") setLoadingPdf(true);
        else setLoadingCsv(true);

        try {
            const res = await api.get(`/api/dashboard/export/${format}`, {
                params: { type },
                responseType: "blob",
            });

            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", `campusseekers_${type}_export.${format}`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);

            toast.success(`Exported ${type} as ${format.toUpperCase()}!`);
        } catch {
            toast.error("Failed to generate export file");
        } finally {
            setLoadingPdf(false);
            setLoadingCsv(false);
        }
    };

    return (
        <Card className="flex flex-col gap-4 p-5 h-full" hoverLift={false} glowColor="rgba(255, 94, 0, 0.05)">
            <div className="flex flex-col gap-0.5 select-none text-left">
                <Badge variant="purple" className="w-fit py-0 px-1.5 text-[8.5px]">Exports Center</Badge>
                <h3 className="text-sm font-bold text-white mt-1">Download Reports</h3>
            </div>

            <div className="flex flex-col gap-2.5">
                <div className="flex flex-col gap-1.5 border-b border-border-color/20 pb-3">
                    <span className="text-[10px] text-text-secondary font-bold uppercase select-none text-left">Dashboard Summary</span>
                    <div className="flex gap-2">
                        <Button variant="secondary" size="sm" className="flex-1 gap-1.5" isLoading={loadingPdf} onClick={() => handleExport("pdf", "dashboard")}>
                            <FileDown className="w-3.5 h-3.5" /> PDF
                        </Button>
                        <Button variant="secondary" size="sm" className="flex-1 gap-1.5" isLoading={loadingCsv} onClick={() => handleExport("csv", "dashboard")}>
                            <FileSpreadsheet className="w-3.5 h-3.5" /> CSV
                        </Button>
                    </div>
                </div>

                <div className="flex flex-col gap-1.5">
                    <span className="text-[10px] text-text-secondary font-bold uppercase select-none text-left">Matched Recommendations</span>
                    <div className="flex gap-2">
                        <Button variant="secondary" size="sm" className="flex-1 gap-1.5" isLoading={loadingPdf} onClick={() => handleExport("pdf", "recommendations")}>
                            <FileDown className="w-3.5 h-3.5" /> PDF
                        </Button>
                        <Button variant="secondary" size="sm" className="flex-1 gap-1.5" isLoading={loadingCsv} onClick={() => handleExport("csv", "recommendations")}>
                            <FileSpreadsheet className="w-3.5 h-3.5" /> CSV
                        </Button>
                    </div>
                </div>
            </div>
        </Card>
    );
}
