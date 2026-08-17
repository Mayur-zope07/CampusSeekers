"use client";

import React, { useState } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
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
    useAdminDashboard,
    useAdminColleges,
    useCreateCollege,
    useUpdateCollege,
    useDeleteCollege,
    useAdminBranches,
    useCreateBranch,
    useUpdateBranch,
    useDeleteBranch,
    useTriggerImport,
    useSystemHealth,
    type CollegeAdminRequest,
    type BranchAdminRequest,
    type CollegeAdminResponse,
    type BranchAdminResponse,
    type ImportSummary,
} from "@/hooks/useAdmin";
import {
    ShieldAlert,
    LayoutDashboard,
    Database,
    UploadCloud,
    Sliders,
    Zap,
    TrendingUp,
    Plus,
    Pencil,
    Trash2,
    Check,
    X,
    Search,
    BookOpen,
    Cpu,
} from "lucide-react";

// ─── Reusable Tabs ────────────────────────────────────────────────────────────

type AdminTab = "dashboard" | "colleges" | "branches" | "imports" | "health";

const ADMIN_NAV = [
    { id: "dashboard", label: "Dashboard",   icon: <LayoutDashboard className="w-4.5 h-4.5" /> },
    { id: "colleges",  label: "Colleges",    icon: <Database className="w-4.5 h-4.5" /> },
    { id: "branches",  label: "Branches",    icon: <BookOpen className="w-4.5 h-4.5" /> },
    { id: "imports",   label: "CSV Imports", icon: <UploadCloud className="w-4.5 h-4.5" /> },
    { id: "health",    label: "System Health",icon: <Cpu className="w-4.5 h-4.5" /> },
];

export default function AdminWorkspacePage() {
    const toast = useToast();
    const [activeTab, setActiveTab] = useState<AdminTab>("dashboard");

    // ─── Query Hook Calls ─────────────────────────────────────────────────────
    const { data: dashboard, isLoading: dashLoading } = useAdminDashboard();
    const { data: health, isLoading: healthLoading } = useSystemHealth();
    const { data: colleges, isLoading: collegesLoading } = useAdminColleges();
    const { data: branches, isLoading: branchesLoading } = useAdminBranches();

    // Mutations
    const { mutate: createCollege, isPending: createColPending } = useCreateCollege();
    const { mutate: updateCollege, isPending: updateColPending } = useUpdateCollege();
    const { mutate: deleteCollege } = useDeleteCollege();
    const { mutate: createBranch, isPending: createBrPending } = useCreateBranch();
    const { mutate: updateBranch, isPending: updateBrPending } = useUpdateBranch();
    const { mutate: deleteBranch } = useDeleteBranch();
    const { mutate: triggerImport, isPending: importPending } = useTriggerImport();

    // ─── Local State for CRUD Forms & Drawers ──────────────────────────────────
    const [searchCol, setSearchCol] = useState("");
    const [searchBr, setSearchBr] = useState("");
    const [editingColId, setEditingColId] = useState<string | null>(null);
    const [showColForm, setShowColForm] = useState(false);
    const [collegeForm, setCollegeForm] = useState<CollegeAdminRequest>({
        name: "", code: "", city: "", state: "", type: "GOVERNMENT",
        naacGrade: "A+", nbaAccredited: true, durationYears: 4,
        intakeCapacity: 120, feesPerYear: 95000, placementRatio: 85,
        averagePackage: 650000, highestPackage: 1500000,
    });

    const [editingBrId, setEditingBrId] = useState<string | null>(null);
    const [showBrForm, setShowBrForm] = useState(false);
    const [branchForm, setBranchForm] = useState<BranchAdminRequest>({
        code: "", name: "", durationYears: 4,
    });

    const [replaceExisting, setReplaceExisting] = useState(false);
    const [importResult, setImportResult] = useState<ImportSummary | null>(null);

    // ─── Handlers ─────────────────────────────────────────────────────────────
    const handleColSave = () => {
        if (!collegeForm.name || !collegeForm.code) {
            toast.error("Name and Code are required.");
            return;
        }
        if (editingColId) {
            updateCollege({ id: editingColId, payload: collegeForm }, {
                onSuccess: () => { toast.success("College updated successfully."); setEditingColId(null); setShowColForm(false); },
                onError: () => toast.error("Failed to update college."),
            });
        } else {
            createCollege(collegeForm, {
                onSuccess: () => { toast.success("College created successfully."); setShowColForm(false); resetColForm(); },
                onError: () => toast.error("Failed to create college."),
            });
        }
    };

    const handleColEdit = (col: CollegeAdminResponse) => {
        setEditingColId(col.id);
        setCollegeForm({
            name: col.name, code: col.code, city: col.city, state: col.state,
            type: col.type || "GOVERNMENT", naacGrade: col.naacGrade || "A",
            nbaAccredited: col.nbaAccredited ?? false, durationYears: col.durationYears || 4,
            intakeCapacity: col.intakeCapacity || 60, feesPerYear: col.feesPerYear || 80000,
            placementRatio: col.placementRatio || 75, averagePackage: col.averagePackage || 500000,
            highestPackage: col.highestPackage || 1200000,
        });
        setShowColForm(true);
    };

    const handleColDelete = (id: string) => {
        if (confirm("Are you sure you want to delete this college record?")) {
            deleteCollege(id, {
                onSuccess: () => toast.success("College deleted successfully."),
                onError: () => toast.error("Failed to delete college."),
            });
        }
    };

    const resetColForm = () => {
        setCollegeForm({
            name: "", code: "", city: "", state: "", type: "GOVERNMENT",
            naacGrade: "A+", nbaAccredited: true, durationYears: 4,
            intakeCapacity: 120, feesPerYear: 95000, placementRatio: 85,
            averagePackage: 650000, highestPackage: 1500000,
        });
        setEditingColId(null);
    };

    const handleBrSave = () => {
        if (!branchForm.name || !branchForm.code) {
            toast.error("Name and Code are required.");
            return;
        }
        if (editingBrId) {
            updateBranch({ id: editingBrId, payload: branchForm }, {
                onSuccess: () => { toast.success("Branch updated successfully."); setEditingBrId(null); setShowBrForm(false); },
                onError: () => toast.error("Failed to update branch."),
            });
        } else {
            createBranch(branchForm, {
                onSuccess: () => { toast.success("Branch created successfully."); setShowBrForm(false); resetBrForm(); },
                onError: () => toast.error("Failed to create branch."),
            });
        }
    };

    const handleBrEdit = (br: BranchAdminResponse) => {
        setEditingBrId(br.id);
        setBranchForm({
            code: br.code, name: br.name, durationYears: br.durationYears || 4,
        });
        setShowBrForm(true);
    };

    const handleBrDelete = (id: string) => {
        if (confirm("Are you sure you want to delete this branch?")) {
            deleteBranch(id, {
                onSuccess: () => toast.success("Branch deleted successfully."),
                onError: () => toast.error("Failed to delete branch."),
            });
        }
    };

    const resetBrForm = () => {
        setBranchForm({ code: "", name: "", durationYears: 4 });
        setEditingBrId(null);
    };

    const handleImport = (type: "all" | "colleges" | "branches" | "college-branches" | "cutoffs" | "seat-matrix") => {
        triggerImport({ type, replace: replaceExisting, dryRun: true }, {
            onSuccess: (data) => {
                setImportResult(data);
                toast.success(`${type.toUpperCase()} import complete.`);
            },
            onError: () => toast.error("Import failed."),
        });
    };

    return (
        <ProtectedRoute role="ADMIN">
            <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-60 pt-24 pb-16 px-6 relative overflow-hidden">
                <Navbar />

                {/* Ambient lights */}
                <div className="absolute inset-0 opacity-[0.02] pointer-events-none z-0">
                    <div className="absolute top-[10%] left-[20%] w-96 h-96 bg-accent-cyan rounded-full blur-[160px]" />
                    <div className="absolute bottom-[20%] right-[20%] w-96 h-96 bg-accent-purple rounded-full blur-[160px]" />
                </div>

                {/* Sidebar Navigation */}
                <aside className="hidden md:flex flex-col h-screen w-60 glass-sidebar fixed left-0 top-0 pt-24 pb-8 px-4 justify-between z-30">
                    <div className="flex flex-col gap-1.5 text-left">
                        <div className="flex items-center gap-2 px-3 py-2 mb-4 select-none">
                            <ShieldAlert className="w-5 h-5 text-accent-orange" />
                            <span className="text-xs font-bold tracking-widest text-white uppercase">Admin Portal</span>
                        </div>
                        {ADMIN_NAV.map(nav => {
                            const active = activeTab === nav.id;
                            return (
                                <button key={nav.id} onClick={() => { setActiveTab(nav.id as AdminTab); setShowColForm(false); setShowBrForm(false); }}
                                    className={`relative flex items-center gap-3 px-4 py-3 text-xs font-semibold rounded-xs transition-all cursor-pointer w-full text-left select-none
                                        ${active ? "bg-white/5 text-white border border-border-color/30" : "text-text-secondary hover:text-white hover:bg-white/2"}`}>
                                    <span className={active ? "text-accent-cyan" : "text-text-tertiary"}>{nav.icon}</span>
                                    {nav.label}
                                    {active && <span className="ml-auto w-1.5 h-1.5 rounded-full bg-accent-cyan shrink-0" />}
                                </button>
                            );
                        })}
                    </div>
                </aside>

                <div className="max-w-5xl mx-auto flex flex-col gap-8 relative z-10 text-left">
                    <ScrollReveal>
                        <div className="flex flex-col gap-2 select-none">
                            <Badge variant="orange" glow className="w-fit text-[9px] px-2.5 py-1">
                                Administrator Control Room
                            </Badge>
                            <h1 className="text-3xl md:text-5xl font-extrabold text-white tracking-tight">
                                Unified{" "}
                                <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan to-accent-purple">
                                    CMS Workspace
                                </span>
                            </h1>
                            <p className="text-xs text-text-secondary max-w-xl leading-relaxed mt-1">
                                Control records, trigger bulk CSV updates, manage academic models and inspect real-time platform system performance.
                            </p>
                        </div>
                    </ScrollReveal>

                    {/* ── Tab Content: Dashboard ── */}
                    {activeTab === "dashboard" && (
                        <div className="flex flex-col gap-8">
                            {dashLoading ? (
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                    {[1, 2, 3, 4].map(x => <Skeleton key={x} className="h-24 w-full" />)}
                                </div>
                            ) : (
                                <ScrollReveal delay={0.05}>
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 select-none">
                                        {[
                                            { label: "Colleges Database", value: dashboard?.collegesCount, icon: <Database className="w-4.5 h-4.5 text-accent-cyan" /> },
                                            { label: "Branch Database", value: dashboard?.branchesCount, icon: <BookOpen className="w-4.5 h-4.5 text-accent-green" /> },
                                            { label: "Matches Run", value: dashboard?.recommendationsCount, icon: <Zap className="w-4.5 h-4.5 text-accent-purple" /> },
                                            { label: "System API Status", value: dashboard?.apiStatus, icon: <TrendingUp className="w-4.5 h-4.5 text-accent-cyan" /> },
                                        ].map((s, idx) => (
                                            <Card key={idx} className="flex flex-col gap-2.5 p-4" hoverLift={false}>
                                                <div className="flex items-center justify-between">
                                                    <span className="text-[9px] font-bold uppercase tracking-wider text-text-tertiary">{s.label}</span>
                                                    {s.icon}
                                                </div>
                                                <span className="text-2xl font-light font-futuristic text-white mt-1">{s.value}</span>
                                            </Card>
                                        ))}
                                    </div>
                                </ScrollReveal>
                            )}

                            {/* Health Preview Card */}
                            <ScrollReveal delay={0.08}>
                                <Card className="p-6 flex flex-col gap-4 text-left border border-border-color/30" hoverLift={false}>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2.5">
                                            <Sliders className="w-4 h-4 text-accent-cyan" />
                                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Quick Actions</span>
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                                        <Button variant="secondary" className="justify-start gap-2" onClick={() => setActiveTab("imports")}>
                                            <UploadCloud className="w-4 h-4 text-accent-cyan" /> CSV Bulk Upload Panel
                                        </Button>
                                        <Button variant="secondary" className="justify-start gap-2" onClick={() => setActiveTab("colleges")}>
                                            <Database className="w-4 h-4 text-accent-purple" /> Manage Institution Profiles
                                        </Button>
                                    </div>
                                </Card>
                            </ScrollReveal>
                        </div>
                    )}

                    {/* ── Tab Content: Colleges ── */}
                    {activeTab === "colleges" && (
                        <div className="flex flex-col gap-6">
                            {!showColForm ? (
                                <>
                                    <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
                                        <div className="relative flex-1 w-full">
                                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-tertiary pointer-events-none" />
                                            <input value={searchCol} onChange={e => setSearchCol(e.target.value)} placeholder="Search by name, city, state or code…"
                                                className="w-full pl-9 pr-4 py-2.5 glass-sm border border-border-color/40 rounded-xs text-[11px] text-white placeholder:text-text-tertiary focus:outline-none focus:border-accent-cyan/50 transition-colors bg-transparent" />
                                        </div>
                                        <Button variant="primary" size="sm" onClick={() => { resetColForm(); setShowColForm(true); }} className="gap-1.5 shrink-0 select-none">
                                            <Plus className="w-4 h-4" /> Add College
                                        </Button>
                                    </div>

                                    {collegesLoading ? (
                                        <div className="flex flex-col gap-3">{[1, 2, 3].map(x => <Skeleton key={x} className="h-16 w-full" />)}</div>
                                    ) : colleges && colleges.length > 0 ? (
                                        <div className="flex flex-col gap-3">
                                            {colleges.filter(c => c.name.toLowerCase().includes(searchCol.toLowerCase()) || c.code.includes(searchCol)).map(col => (
                                                <Card key={col.id} className="p-4 flex items-center justify-between gap-4 border border-border-color/20 text-left" hoverLift={false}>
                                                    <div className="min-w-0">
                                                        <div className="flex items-center gap-2 flex-wrap">
                                                            <Badge variant="cyan" className="text-[8.5px] px-1.5 py-0">{col.code}</Badge>
                                                            <Badge variant="purple" className="text-[8.5px] px-1.5 py-0">{col.type}</Badge>
                                                            <span className="text-xs font-bold text-white truncate">{col.name}</span>
                                                        </div>
                                                        <span className="text-[10px] text-text-tertiary block mt-0.5">{col.city}, {col.state} · NAAC: {col.naacGrade}</span>
                                                    </div>
                                                    <div className="flex gap-1.5 shrink-0">
                                                        <button onClick={() => handleColEdit(col)} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-cyan/40 transition-colors cursor-pointer">
                                                            <Pencil className="w-3.5 h-3.5 text-text-secondary" />
                                                        </button>
                                                        <button onClick={() => handleColDelete(col.id)} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-orange/40 transition-colors cursor-pointer">
                                                            <Trash2 className="w-3.5 h-3.5 text-accent-orange" />
                                                        </button>
                                                    </div>
                                                </Card>
                                            ))}
                                        </div>
                                    ) : (
                                        <EmptyState title="No college records" description="Database is empty. Populate with a manual entry or via CSV import." />
                                    )}
                                </>
                            ) : (
                                <Card className="p-6 flex flex-col gap-4 text-left border border-accent-cyan/25" hoverLift={false}>
                                    <span className="text-[10px] font-bold uppercase tracking-wider text-accent-cyan">{editingColId ? "Edit College Record" : "Add College Record"}</span>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                        <Input label="Name *" value={collegeForm.name} onChange={e => setCollegeForm({ ...collegeForm, name: e.target.value })} required />
                                        <Input label="Code *" value={collegeForm.code} onChange={e => setCollegeForm({ ...collegeForm, code: e.target.value })} required />
                                        <Input label="City *" value={collegeForm.city} onChange={e => setCollegeForm({ ...collegeForm, city: e.target.value })} />
                                        <Input label="State *" value={collegeForm.state} onChange={e => setCollegeForm({ ...collegeForm, state: e.target.value })} />
                                        <Dropdown options={[{ label: "GOVERNMENT", value: "GOVERNMENT" }, { label: "PRIVATE", value: "PRIVATE" }]} selected={collegeForm.type} onChange={v => setCollegeForm({ ...collegeForm, type: v as "GOVERNMENT" | "PRIVATE" })} />
                                        <Input label="NAAC Grade" value={collegeForm.naacGrade} onChange={e => setCollegeForm({ ...collegeForm, naacGrade: e.target.value })} />
                                        <Input label="Intake Capacity" type="number" value={String(collegeForm.intakeCapacity)} onChange={e => setCollegeForm({ ...collegeForm, intakeCapacity: Number(e.target.value) })} />
                                        <Input label="Fees per Year (INR)" type="number" value={String(collegeForm.feesPerYear)} onChange={e => setCollegeForm({ ...collegeForm, feesPerYear: Number(e.target.value) })} />
                                    </div>
                                    <div className="flex gap-2.5 pt-2 border-t border-border-color/20 mt-4">
                                        <Button variant="primary" size="sm" isLoading={createColPending || updateColPending} onClick={handleColSave}>
                                            <Check className="w-3.5 h-3.5 mr-1" /> Save
                                        </Button>
                                        <Button variant="secondary" size="sm" onClick={() => setShowColForm(false)}>
                                            <X className="w-3.5 h-3.5 mr-1" /> Cancel
                                        </Button>
                                    </div>
                                </Card>
                            )}
                        </div>
                    )}

                    {/* ── Tab Content: Branches ── */}
                    {activeTab === "branches" && (
                        <div className="flex flex-col gap-6">
                            {!showBrForm ? (
                                <>
                                    <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
                                        <div className="relative flex-1 w-full">
                                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-tertiary pointer-events-none" />
                                            <input value={searchBr} onChange={e => setSearchBr(e.target.value)} placeholder="Search branches by name or code…"
                                                className="w-full pl-9 pr-4 py-2.5 glass-sm border border-border-color/40 rounded-xs text-[11px] text-white placeholder:text-text-tertiary focus:outline-none focus:border-accent-cyan/50 transition-colors bg-transparent" />
                                        </div>
                                        <Button variant="primary" size="sm" onClick={() => { resetBrForm(); setShowBrForm(true); }} className="gap-1.5 shrink-0 select-none">
                                            <Plus className="w-4 h-4" /> Add Branch
                                        </Button>
                                    </div>

                                    {branchesLoading ? (
                                        <div className="flex flex-col gap-3">{[1, 2, 3].map(x => <Skeleton key={x} className="h-16 w-full" />)}</div>
                                    ) : branches && branches.length > 0 ? (
                                        <div className="flex flex-col gap-3">
                                            {branches.filter(b => b.name.toLowerCase().includes(searchBr.toLowerCase()) || b.code.includes(searchBr)).map(br => (
                                                <Card key={br.id} className="p-4 flex items-center justify-between gap-4 border border-border-color/20 text-left" hoverLift={false}>
                                                    <div className="min-w-0">
                                                        <div className="flex items-center gap-2 flex-wrap">
                                                            <Badge variant="cyan" className="text-[8.5px] px-1.5 py-0">{br.code}</Badge>
                                                            <span className="text-xs font-bold text-white truncate">{br.name}</span>
                                                        </div>
                                                        <span className="text-[10px] text-text-tertiary block mt-0.5">Duration: {br.durationYears} Years</span>
                                                    </div>
                                                    <div className="flex gap-1.5 shrink-0">
                                                        <button onClick={() => handleBrEdit(br)} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-cyan/40 transition-colors cursor-pointer">
                                                            <Pencil className="w-3.5 h-3.5 text-text-secondary" />
                                                        </button>
                                                        <button onClick={() => handleBrDelete(br.id)} className="w-8 h-8 rounded-xs border border-border-color/30 bg-white/3 flex items-center justify-center hover:border-accent-orange/40 transition-colors cursor-pointer">
                                                            <Trash2 className="w-3.5 h-3.5 text-accent-orange" />
                                                        </button>
                                                    </div>
                                                </Card>
                                            ))}
                                        </div>
                                    ) : (
                                        <EmptyState title="No branches found" description="Add manual branch entries or use import system." />
                                    )}
                                </>
                            ) : (
                                <Card className="p-6 flex flex-col gap-4 text-left border border-accent-cyan/25" hoverLift={false}>
                                    <span className="text-[10px] font-bold uppercase tracking-wider text-accent-cyan">{editingBrId ? "Edit Branch" : "Add Branch"}</span>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                        <Input label="Name *" value={branchForm.name} onChange={e => setBranchForm({ ...branchForm, name: e.target.value })} required />
                                        <Input label="Code *" value={branchForm.code} onChange={e => setBranchForm({ ...branchForm, code: e.target.value })} required />
                                        <Input label="Duration Years" type="number" value={String(branchForm.durationYears)} onChange={e => setBranchForm({ ...branchForm, durationYears: Number(e.target.value) })} />
                                    </div>
                                    <div className="flex gap-2.5 pt-2 border-t border-border-color/20 mt-4">
                                        <Button variant="primary" size="sm" isLoading={createBrPending || updateBrPending} onClick={handleBrSave}>
                                            <Check className="w-3.5 h-3.5 mr-1" /> Save
                                        </Button>
                                        <Button variant="secondary" size="sm" onClick={() => setShowBrForm(false)}>
                                            <X className="w-3.5 h-3.5 mr-1" /> Cancel
                                        </Button>
                                    </div>
                                </Card>
                            )}
                        </div>
                    )}

                    {/* ── Tab Content: Imports ── */}
                    {activeTab === "imports" && (
                        <div className="flex flex-col gap-6">
                            <Card className="p-6 flex flex-col gap-4 text-left" hoverLift={false}>
                                <div className="flex items-center justify-between">
                                    <div className="flex flex-col">
                                        <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Bulk CSV Import Panel</span>
                                        <p className="text-[10.5px] text-text-tertiary mt-0.5">Triggers batch file processors preloaded on server directory paths.</p>
                                    </div>
                                    <div className="flex items-center gap-2 select-none">
                                        <input type="checkbox" checked={replaceExisting} onChange={e => setReplaceExisting(e.target.checked)} id="replaceCheckbox"
                                            className="w-3.5 h-3.5 accent-accent-cyan cursor-pointer" />
                                        <label htmlFor="replaceCheckbox" className="text-[11px] text-text-secondary cursor-pointer">Replace Existing</label>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 md:grid-cols-3 gap-3 pt-2">
                                    {([
                                        { label: "Import Colleges", type: "colleges" },
                                        { label: "Import Branches", type: "branches" },
                                        { label: "Import Mapping",  type: "college-branches" },
                                        { label: "Import Cutoffs",  type: "cutoffs" },
                                        { label: "Import Seat Matrix", type: "seat-matrix" },
                                        { label: "Import All Datasets", type: "all" },
                                    ] as const).map(btn => (
                                        <Button key={btn.type} variant="secondary" size="sm" isLoading={importPending} onClick={() => handleImport(btn.type)} className="justify-center select-none font-bold">
                                            {btn.label}
                                        </Button>
                                    ))}
                                </div>
                            </Card>

                            {importResult && (
                                <ScrollReveal>
                                    <Card className="p-6 flex flex-col gap-4 border border-accent-green/20 text-left bg-accent-green/3" hoverLift={false}>
                                        <div className="flex items-center gap-2">
                                            <span className="text-[10px] font-bold uppercase tracking-wider text-accent-green">Execution Log</span>
                                            <Badge variant="green" className="text-[8px] py-0">{importResult.status}</Badge>
                                        </div>
                                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-[11px] pt-1">
                                            <div><span className="text-text-tertiary block">Processed</span><span className="font-semibold text-white">{importResult.rowsProcessed}</span></div>
                                            <div><span className="text-text-tertiary block">Inserted</span><span className="font-semibold text-accent-green">{importResult.rowsInserted}</span></div>
                                            <div><span className="text-text-tertiary block">Updated</span><span className="font-semibold text-white">{importResult.rowsUpdated}</span></div>
                                            <div><span className="text-text-tertiary block">Execution Time</span><span className="font-semibold text-white">{importResult.executionTime || "N/A"}</span></div>
                                        </div>
                                        {importResult.validationErrors?.length > 0 && (
                                            <div className="mt-2 pt-2 border-t border-border-color/20 flex flex-col gap-1">
                                                <span className="text-[10px] font-bold text-accent-orange">Validation Errors</span>
                                                {importResult.validationErrors.slice(0, 3).map((err: string, i: number) => (
                                                    <span key={i} className="text-[9px] text-text-secondary leading-relaxed block">• {err}</span>
                                                ))}
                                            </div>
                                        )}
                                    </Card>
                                </ScrollReveal>
                            )}
                        </div>
                    )}

                    {/* ── Tab Content: Health ── */}
                    {activeTab === "health" && (
                        <div className="flex flex-col gap-6">
                            {healthLoading ? (
                                <Skeleton className="h-64 w-full" />
                            ) : (
                                <ScrollReveal>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                        {/* Status */}
                                        <Card className="p-6 flex flex-col gap-4 text-left" hoverLift={false}>
                                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Operational Metrics</span>
                                            <div className="flex flex-col gap-3">
                                                {[
                                                    { label: "Engine Status", value: health?.dbStatus, variant: "green" },
                                                    { label: "API Gateway",   value: health?.apiStatus, variant: "green" },
                                                    { label: "Model Version",  value: health?.engineVersion, variant: "cyan" },
                                                    { label: "Cache Rate",    value: health?.cacheHitRate, variant: "cyan" },
                                                ].map(item => (
                                                    <div key={item.label} className="flex justify-between items-center text-xs">
                                                        <span className="text-text-secondary">{item.label}</span>
                                                        <Badge variant={item.variant as "green" | "cyan" | "purple" | "orange" | "default"} className="text-[9px]">{item.value}</Badge>
                                                    </div>
                                                ))}
                                            </div>
                                        </Card>

                                        {/* Memory */}
                                        <Card className="p-6 flex flex-col gap-4 text-left" hoverLift={false}>
                                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Node Resource Allocation</span>
                                            <div className="flex flex-col gap-3">
                                                <div className="flex justify-between items-center text-xs">
                                                    <span className="text-text-secondary">Allocated Memory</span>
                                                    <span className="font-semibold text-white">{health?.memoryUsage}</span>
                                                </div>
                                                <div className="flex justify-between items-center text-xs">
                                                    <span className="text-text-secondary">Average CPU load</span>
                                                    <span className="font-semibold text-white">{health?.cpuLoad}</span>
                                                </div>
                                            </div>
                                        </Card>
                                    </div>
                                </ScrollReveal>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </ProtectedRoute>
    );
}
