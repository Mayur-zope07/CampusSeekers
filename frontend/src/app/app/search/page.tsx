"use client";

import React, { useState } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { Dialog } from "@/components/ui/Dialog";
import { Dropdown } from "@/components/ui/Dropdown";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useColleges, useSearch, useComparison } from "@/hooks/useCollegeSearch";
import { useToast } from "@/providers/ToastProvider";
import { useCreateWishlist } from "@/hooks/useWorkflow";
import {
    ArrowRight,
    Heart,
    Scale,
    Sliders,
    X,
    Maximize2
} from "lucide-react";
import { useRouter } from "next/navigation";

export default function CollegeSearchPage() {
    const router = useRouter();
    const toast = useToast();
    const [sidebarItem, setSidebarItem] = useState("search");

    // Unified Toggles: "standard" vs "eligibility"
    const [searchMode, setSearchMode] = useState<"standard" | "eligibility">("standard");

    // Form Filter States
    const [keyword, setKeyword] = useState("");
    const [city, setCity] = useState("");
    const state = "";
    const [naacGrade, setNaacGrade] = useState("ALL");
    const [collegeType, setCollegeType] = useState("ALL");

    // Eligibility percentile search states
    const [examName, setExamName] = useState("MHT_CET");
    const [examYear, setExamYear] = useState("2024");
    const [percentileVal, setPercentileVal] = useState("95.0");
    const [category, setCategory] = useState("OPEN");

    // Comparison Selection states
    const [compareIds, setCompareIds] = useState<string[]>([]);
    const [isCompareOpen, setIsCompareOpen] = useState(false);

    React.useEffect(() => {
        if (typeof window !== "undefined") {
            const params = new URLSearchParams(window.location.search);
            const queryParam = params.get("query");
            if (queryParam) {
                setKeyword(queryParam);
            }
        }
    }, []);

    // Right Side preview drawer states
    const [previewCollegeId, setPreviewCollegeId] = useState<string | null>(null);
    const [previewDetails, setPreviewDetails] = useState<{ id?: string; name?: string; city?: string; state?: string; naacGrade?: string; type?: string; code?: string; averagePackage?: number } | null>(null);

    // Queries
    const standardParams = {
        keyword: keyword || undefined,
        city: city || undefined,
        state: state || undefined,
        naacGrade: naacGrade === "ALL" ? undefined : naacGrade,
        type: collegeType === "ALL" ? undefined : collegeType,
        page: 0,
        size: 15
    };

    const eligibilityParams = {
        exam: examName,
        year: Number(examYear),
        percentile: Number(percentileVal) || 95.0,
        category: category,
        city: city || undefined,
        state: state || undefined,
        page: 0,
        size: 15
    };

    const { data: standardData, isLoading: standardLoading } = useColleges(standardParams);
    const { data: eligibilityData, isLoading: eligibilityLoading } = useSearch(eligibilityParams, searchMode === "eligibility");
    const { data: comparisonData, isLoading: comparisonLoading } = useComparison(compareIds, isCompareOpen && compareIds.length > 0);

    const isLoading = searchMode === "standard" ? standardLoading : eligibilityLoading;
    const results = searchMode === "standard" ? standardData?.content : eligibilityData?.content;

    // Toggle comparison college
    const toggleCompare = (id: string) => {
        if (compareIds.includes(id)) {
            setCompareIds(compareIds.filter((cid) => cid !== id));
        } else {
            if (compareIds.length >= 5) {
                toast.error("Can compare up to 5 colleges side by side.");
                return;
            }
            setCompareIds([...compareIds, id]);
        }
    };

    // Open side preview drawer
    const openPreview = (college: { id: string; name: string; city: string; state: string; naacGrade?: string; type?: string; code?: string; averagePackage?: number }) => {
        setPreviewCollegeId(college.id);
        setPreviewDetails(college);
    };

    const { mutate: createWishlist } = useCreateWishlist();

    const handleWishlist = (collegeId: string, name: string) => {
        createWishlist(collegeId, {
            onSuccess: () => toast.success(`Successfully saved ${name} to wishlist!`),
            onError: (err: unknown) => {
                const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
                toast.error(msg ?? "Already in wishlist or failed to save.");
            },
        });
    };

    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-64 pt-24 pb-16 px-6 relative overflow-hidden">
                <Navbar />
                <Sidebar activeItem={sidebarItem} onChange={setSidebarItem} />

                <div className="max-w-5xl mx-auto flex flex-col gap-6 relative z-10">
                    {/* Header */}
                    <div className="flex flex-col gap-1 select-none text-left">
                        <Badge variant="cyan" className="w-fit py-0 px-2 text-[9px]">Flagship Discovery</Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white mt-1">College Finder</h1>
                    </div>

                    {/* Mode Toggle Buttons */}
                    <div className="flex gap-4 border-b border-border-color/20 pb-4 select-none">
                        <Button
                            variant={searchMode === "standard" ? "primary" : "secondary"}
                            size="sm"
                            onClick={() => setSearchMode("standard")}
                        >
                            Standard Explorer
                        </Button>
                        <Button
                            variant={searchMode === "eligibility" ? "primary" : "secondary"}
                            size="sm"
                            onClick={() => setSearchMode("eligibility")}
                        >
                            Percentile Eligibility Matcher
                        </Button>
                    </div>

                    {/* Left Filters + Center Results grid */}
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6 items-start">
                        {/* Sidebar Filters */}
                        <div className="flex flex-col gap-4">
                            <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false} glowColor="rgba(0, 240, 255, 0.05)">
                                <div className="flex items-center gap-2 select-none border-b border-border-color/20 pb-2.5">
                                    <Sliders className="w-4 h-4 text-accent-cyan" />
                                    <span className="text-xs font-bold text-white uppercase tracking-wider">Search Filters</span>
                                </div>

                                {searchMode === "standard" ? (
                                    <div className="flex flex-col gap-4">
                                        <Input label="Query Colleges" placeholder="Search course codes..." value={keyword} onChange={(e) => setKeyword(e.target.value)} />
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">NAAC Grade</label>
                                            <Dropdown
                                                options={[
                                                    { label: "All Grades", value: "ALL" },
                                                    { label: "A++ Grade", value: "A++" },
                                                    { label: "A+ Grade", value: "A+" },
                                                    { label: "A Grade", value: "A" }
                                                ]}
                                                selected={naacGrade}
                                                onChange={setNaacGrade}
                                            />
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Type</label>
                                            <Dropdown
                                                options={[
                                                    { label: "All Affiliations", value: "ALL" },
                                                    { label: "Government", value: "GOVERNMENT" },
                                                    { label: "Private Un-Aided", value: "PRIVATE" }
                                                ]}
                                                selected={collegeType}
                                                onChange={setCollegeType}
                                            />
                                        </div>
                                    </div>
                                ) : (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Exam</label>
                                            <Dropdown
                                                options={[
                                                    { label: "MHT-CET", value: "MHT_CET" },
                                                    { label: "JEE Main", value: "JEE_MAIN" }
                                                ]}
                                                selected={examName}
                                                onChange={setExamName}
                                            />
                                        </div>
                                        <Input label="Exam Year" type="number" value={examYear} onChange={(e) => setExamYear(e.target.value)} />
                                        <Input label="Percentile Score" type="number" value={percentileVal} onChange={(e) => setPercentileVal(e.target.value)} />
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Category</label>
                                            <Dropdown
                                                options={[
                                                    { label: "OPEN", value: "OPEN" },
                                                    { label: "OBC", value: "OBC" },
                                                    { label: "SC", value: "SC" },
                                                    { label: "ST", value: "ST" },
                                                    { label: "EWS", value: "EWS" }
                                                ]}
                                                selected={category}
                                                onChange={setCategory}
                                            />
                                        </div>
                                    </div>
                                )}

                                <div className="border-t border-border-color/20 pt-3">
                                    <Input label="City / Region" placeholder="e.g. Pune" value={city} onChange={(e) => setCity(e.target.value)} />
                                </div>
                            </Card>
                        </div>

                        {/* Results Grid column */}
                        <div className="md:col-span-3 flex flex-col gap-4">
                            {isLoading ? (
                                <div className="flex flex-col gap-4">
                                    {[1, 2, 3].map((x) => (
                                        <Skeleton key={x} className="h-28 w-full animate-pulse" />
                                    ))}
                                </div>
                            ) : results && results.length > 0 ? (
                                <div className="flex flex-col gap-4">
                                    {results.map((college: { id: string; name: string; city: string; state: string; naacGrade?: string; type?: string; code?: string; averagePackage?: number; highestPackage?: number }, idx: number) => (
                                        <ScrollReveal key={college.id || idx} delay={idx * 0.05}>
                                            <Card className="flex flex-col gap-3 p-5 text-left relative" glowColor="rgba(0, 240, 255, 0.03)" hoverLift={false}>
                                                <div className="flex justify-between items-start select-none">
                                                    <div className="flex flex-col gap-0.5">
                                                        <div className="flex items-center gap-2">
                                                            <Badge variant="purple" className="py-0 px-1 text-[9.5px]">NAAC {college.naacGrade || "A"}</Badge>
                                                            <span className="font-semibold text-sm text-white">{college.name}</span>
                                                        </div>
                                                        <span className="text-[10px] text-text-tertiary">{college.city}, {college.state} • {college.type || "Government"}</span>
                                                    </div>
                                                    <div className="flex gap-2">
                                                        <button onClick={() => handleWishlist(college.id, college.name)} className="w-8 h-8 rounded-full border border-border-color hover:border-accent-orange/40 hover:bg-accent-orange/5 text-text-secondary hover:text-accent-orange flex items-center justify-center transition-all cursor-pointer">
                                                            <Heart className="w-4 h-4" />
                                                        </button>
                                                        <button onClick={() => toggleCompare(college.id)} className={`w-8 h-8 rounded-full border flex items-center justify-center transition-all cursor-pointer ${compareIds.includes(college.id) ? "border-accent-cyan bg-accent-cyan/10 text-accent-cyan" : "border-border-color hover:border-accent-cyan/40 hover:bg-accent-cyan/5 text-text-secondary hover:text-accent-cyan"}`}>
                                                            <Scale className="w-4 h-4" />
                                                        </button>
                                                    </div>
                                                </div>

                                                <div className="grid grid-cols-3 gap-4 border-t border-border-color/20 pt-3 text-[11px] text-text-secondary">
                                                    <div>
                                                        <span className="text-text-tertiary block">Highest Package</span>
                                                        <span className="font-semibold text-white">₹{college.highestPackage ? (college.highestPackage / 100000).toFixed(1) : "12.0"} L</span>
                                                    </div>
                                                    <div>
                                                        <span className="text-text-tertiary block">Average Package</span>
                                                        <span className="font-semibold text-white">₹{college.averagePackage ? (college.averagePackage / 100000).toFixed(1) : "6.5"} L</span>
                                                    </div>
                                                    <div className="text-right">
                                                        <button onClick={() => openPreview(college)} className="text-accent-cyan font-bold hover:underline gap-1 inline-flex items-center cursor-pointer select-none">
                                                            Quick Preview <ArrowRight className="w-3 h-3" />
                                                        </button>
                                                    </div>
                                                </div>
                                            </Card>
                                        </ScrollReveal>
                                    ))}
                                </div>
                            ) : (
                                <EmptyState
                                    title="No Colleges Found"
                                    description="No colleges fit the selected filter parameters. Adjust city or grade parameters."
                                />
                            )}
                        </div>
                    </div>
                </div>

                {/* Floating Compare trigger action bar */}
                {compareIds.length > 0 && (
                    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-40 bg-neutral-900/90 backdrop-blur-md px-6 py-3 border border-border-color rounded-full shadow-2xl flex items-center gap-6 text-xs select-none">
                        <span className="text-white font-semibold">{compareIds.length} Colleges selected</span>
                        <div className="flex gap-2">
                            <Button variant="secondary" size="sm" onClick={() => setCompareIds([])}>
                                Reset
                            </Button>
                            <Button variant="primary" size="sm" onClick={() => setIsCompareOpen(true)}>
                                Compare Now
                            </Button>
                        </div>
                    </div>
                )}

                {/* Compare Dialog overlay */}
                <Dialog isOpen={isCompareOpen} onClose={() => setIsCompareOpen(false)} title="Colleges Comparison matrix">
                    <div className="flex flex-col gap-4 mt-2">
                        {comparisonLoading ? (
                            <div className="flex flex-col gap-2.5">
                                <Skeleton className="h-8 w-full animate-pulse" />
                                <Skeleton className="h-20 w-full animate-pulse" />
                            </div>
                        ) : comparisonData ? (
                            <div className="border border-border-color/30 rounded-md overflow-x-auto bg-white/2 text-[11px]">
                                <table className="w-full border-collapse">
                                    <thead>
                                        <tr className="border-b border-border-color/20 text-white bg-white/5 font-bold">
                                            <th className="p-3 text-left">Metrics</th>
                                            {comparisonData.colleges?.map((col: { id: string; name: string }) => (
                                                <th key={col.id} className="p-3 text-left min-w-[120px]">{col.name}</th>
                                            ))}
                                        </tr>
                                    </thead>
                                    <tbody className="text-text-secondary">
                                        <tr className="border-b border-border-color/10">
                                            <td className="p-3 font-semibold text-white">City Location</td>
                                            {comparisonData.colleges?.map((col: { id: string; city?: string }) => (
                                                <td key={col.id} className="p-3">{col.city}</td>
                                            ))}
                                        </tr>
                                        <tr className="border-b border-border-color/10">
                                            <td className="p-3 font-semibold text-white">NAAC Accreditation</td>
                                            {comparisonData.colleges?.map((col: { id: string; naacGrade?: string }) => (
                                                <td key={col.id} className="p-3">{col.naacGrade || "A"}</td>
                                            ))}
                                        </tr>
                                        <tr className="border-b border-border-color/10">
                                            <td className="p-3 font-semibold text-white">Govt / Private</td>
                                            {comparisonData.colleges?.map((col: { id: string; type?: string }) => (
                                                <td key={col.id} className="p-3">{col.type || "Government"}</td>
                                            ))}
                                        </tr>
                                        <tr>
                                            <td className="p-3 font-semibold text-white">Avg Package / Yr</td>
                                            {comparisonData.colleges?.map((col: { id: string; averagePackage?: number }) => (
                                                <td key={col.id} className="p-3">₹{col.averagePackage ? (col.averagePackage / 100000).toFixed(1) : "6.5"} L</td>
                                            ))}
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        ) : (
                            <div className="text-center text-text-secondary select-none">
                                Failed to fetch comparison records.
                            </div>
                        )}
                        <div className="flex justify-end gap-2 mt-4">
                            <Button variant="secondary" onClick={() => setIsCompareOpen(false)}>
                                Close Matrix
                            </Button>
                        </div>
                    </div>
                </Dialog>

                {/* Right Preview Drawer panel overlay */}
                {previewCollegeId && (
                    <div className="fixed inset-y-0 right-0 w-full sm:w-[400px] z-50 bg-[#08080B]/95 border-l border-border-color shadow-2xl p-6 flex flex-col justify-between select-none">
                        <div className="flex flex-col gap-4">
                            <div className="flex justify-between items-center border-b border-border-color/20 pb-3">
                                <span className="text-xs font-bold text-white uppercase tracking-wider">Quick Details</span>
                                <button onClick={() => setPreviewCollegeId(null)} className="text-text-secondary hover:text-white transition-colors cursor-pointer">
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            {previewDetails && (
                                <div className="flex flex-col gap-4 text-left">
                                    <div className="flex flex-col gap-1">
                                        <Badge variant="purple" className="w-fit">NAAC {previewDetails.naacGrade || "A"}</Badge>
                                        <h4 className="text-sm font-bold text-white leading-tight mt-1">{previewDetails.name}</h4>
                                        <span className="text-[10px] text-text-tertiary">{previewDetails.city}, {previewDetails.state}</span>
                                    </div>

                                    <div className="border border-border-color/20 rounded-md p-4 bg-white/2 flex flex-col gap-3 text-xs text-text-secondary">
                                        <div className="flex justify-between">
                                            <span>College Code</span>
                                            <span className="text-white font-semibold">{previewDetails.code || "1002"}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span>Affiliation Type</span>
                                            <span className="text-white font-semibold">{previewDetails.type || "Government"}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span>Average Placements</span>
                                            <span className="text-accent-green font-bold">₹{previewDetails.averagePackage ? (previewDetails.averagePackage / 100000).toFixed(1) : "6.5"} L</span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="flex flex-col gap-2 mt-6">
                            <Button variant="primary" className="w-full" onClick={() => router.push(`/app/colleges/${previewCollegeId}`)}>
                                Open Full College Profile <Maximize2 className="w-4 h-4 ml-1.5 shrink-0" />
                            </Button>
                            <Button variant="secondary" className="w-full" onClick={() => setPreviewCollegeId(null)}>
                                Close Preview
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        </ProtectedRoute>
    );
}
