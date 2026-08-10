"use client";

import React, { useState } from "react";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Dialog } from "@/components/ui/Dialog";
import { Drawer } from "@/components/ui/Drawer";
import { Dropdown } from "@/components/ui/Dropdown";
import { Tabs } from "@/components/ui/Tabs";
import { Accordion } from "@/components/ui/Accordion";
import { Badge } from "@/components/ui/Badge";
import { ProgressBar } from "@/components/ui/ProgressBar";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { Tooltip } from "@/components/ui/Tooltip";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { Magnetic } from "@/components/animations/Magnetic";
import { useToast } from "@/providers/ToastProvider";
import {
    Sparkles,
    Search,
    UserCheck,
    ArrowUpRight
} from "lucide-react";

export default function DesignSystemShowcase() {
    const toast = useToast();
    const [activeSidebar, setActiveSidebar] = useState("dashboard");
    const [activeTab, setActiveTab] = useState("general");
    const [dropdownVal, setDropdownVal] = useState("cyan");
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);
    const [loadingBtn, setLoadingBtn] = useState(false);

    const triggerLoading = () => {
        setLoadingBtn(true);
        setTimeout(() => {
            setLoadingBtn(false);
            toast.success("Transaction completed successfully!");
        }, 2000);
    };

    return (
        <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-64 pt-24 pb-16 px-6">
            <Navbar />
            <Sidebar activeItem={activeSidebar} onChange={setActiveSidebar} />

            <div className="max-w-5xl mx-auto flex flex-col gap-12 relative z-10">
                {/* Hero Header */}
                <ScrollReveal>
                    <div className="flex flex-col gap-2">
                        <Badge variant="cyan" glow className="w-fit">Phase 10.0</Badge>
                        <h1 className="text-3xl md:text-5xl font-futuristic font-extralight tracking-[0.2em] text-white uppercase select-none mt-2">
                            Design System
                        </h1>
                        <p className="text-sm text-text-secondary max-w-xl select-none mt-1 leading-relaxed">
                            The Apple-inspired, high-fidelity design language and interactive token foundation of CampusSeekers.
                        </p>
                    </div>
                </ScrollReveal>

                {/* Grid layout */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {/* Buttons Section */}
                    <ScrollReveal delay={0.1}>
                        <Card className="flex flex-col gap-6" glowColor="rgba(0, 240, 255, 0.15)">
                            <div>
                                <h3 className="text-sm font-bold text-white tracking-wide uppercase select-none mb-1">Tactile Button System</h3>
                                <p className="text-xs text-text-tertiary select-none">Animated springs, glows, loading status, and magnetic attractors.</p>
                            </div>
                            <div className="flex flex-wrap gap-4 items-center">
                                <Magnetic>
                                    <Button variant="primary">Primary Click</Button>
                                </Magnetic>
                                <Button variant="secondary">Secondary</Button>
                                <Button variant="accent">Accent</Button>
                                <Button variant="danger">Danger</Button>
                            </div>
                            <div className="flex flex-wrap gap-4 items-center border-t border-border-color/30 pt-4">
                                <Button variant="primary" isLoading={loadingBtn} onClick={triggerLoading}>
                                    Async Action
                                </Button>
                                <Button variant="secondary" isSuccess>Success</Button>
                                <Button variant="secondary" isError>Error</Button>
                            </div>
                        </Card>
                    </ScrollReveal>

                    {/* Inputs Section */}
                    <ScrollReveal delay={0.2}>
                        <Card className="flex flex-col gap-6" glowColor="rgba(138, 43, 226, 0.15)">
                            <div>
                                <h3 className="text-sm font-bold text-white tracking-wide uppercase select-none mb-1">macOS Fields</h3>
                                <p className="text-xs text-text-tertiary select-none">Floating validations, animated focus boundaries, and custom search inputs.</p>
                            </div>
                            <div className="flex flex-col gap-4">
                                <Input label="Full Name" placeholder="Enter your full name" icon={<UserCheck className="w-4 h-4" />} />
                                <Input label="Query Colleges" placeholder="Search course tags..." icon={<Search className="w-4 h-4" />} error="College branch is currently matching 12 other cutoffs" />
                            </div>
                        </Card>
                    </ScrollReveal>

                    {/* Interactive Glass Modules */}
                    <ScrollReveal delay={0.3}>
                        <Card className="flex flex-col gap-6" glowColor="rgba(57, 255, 20, 0.12)">
                            <div>
                                <h3 className="text-sm font-bold text-white tracking-wide uppercase select-none mb-1">Interactions & Toasts</h3>
                                <p className="text-xs text-text-tertiary select-none">Framer motion slide overlays, triggers, and state managers.</p>
                            </div>
                            <div className="flex flex-wrap gap-4">
                                <Button variant="secondary" onClick={() => setIsDialogOpen(true)}>Open Dialog</Button>
                                <Button variant="secondary" onClick={() => setIsDrawerOpen(true)}>Open Drawer</Button>
                            </div>
                            <div className="flex flex-wrap gap-2 pt-2 border-t border-border-color/30">
                                <Button variant="secondary" size="sm" onClick={() => toast.success("Connected to postgres dataset!")}>
                                    Toast Success
                                </Button>
                                <Button variant="secondary" size="sm" onClick={() => toast.error("Opt-lock conflict encountered!")}>
                                    Toast Error
                                </Button>
                                <Button variant="secondary" size="sm" onClick={() => toast.info("Cache entry evicted!")}>
                                    Toast Info
                                </Button>
                            </div>
                        </Card>
                    </ScrollReveal>

                    {/* Navigation Components */}
                    <ScrollReveal delay={0.4}>
                        <Card className="flex flex-col gap-6">
                            <div>
                                <h3 className="text-sm font-bold text-white tracking-wide uppercase select-none mb-1">Navigation & Menus</h3>
                                <p className="text-xs text-text-tertiary select-none">Layout sliding pills and reactive dropdown selections.</p>
                            </div>
                            <div className="flex flex-col gap-4">
                                <Tabs
                                    tabs={[
                                        { id: "general", label: "General Config" },
                                        { id: "rules", label: "Business Rules" },
                                        { id: "security", label: "Security ACL" }
                                    ]}
                                    activeTab={activeTab}
                                    onChange={setActiveTab}
                                />
                                <Dropdown
                                    options={[
                                        { label: "Electric Cyan", value: "cyan", icon: <Sparkles className="text-accent-cyan w-4 h-4" /> },
                                        { label: "Royal Purple", value: "purple", icon: <Sparkles className="text-accent-purple w-4 h-4" /> },
                                        { label: "Aurora Green", value: "green", icon: <Sparkles className="text-accent-green w-4 h-4" /> }
                                    ]}
                                    selected={dropdownVal}
                                    onChange={setDropdownVal}
                                />
                            </div>
                        </Card>
                    </ScrollReveal>
                </div>

                {/* Additional Showcase Components */}
                <ScrollReveal delay={0.5}>
                    <Card className="flex flex-col gap-6">
                        <div>
                            <h3 className="text-sm font-bold text-white tracking-wide uppercase select-none mb-1">Layout Details & Metrics</h3>
                            <p className="text-xs text-text-tertiary select-none">Accordions, tooltips, progress bars, and glowing skeletons.</p>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div className="flex flex-col gap-4">
                                <Accordion
                                    items={[
                                        { id: "a1", title: "How is matching computed?", content: "Matching is computed deterministically using historical cutoffs, round specifications, and score differences." },
                                        { id: "a2", title: "Can I adjust weight multipliers?", content: "Yes! Multitier criteria parameters can be adjusted via backend configurations without recompilation." }
                                    ]}
                                />
                                <div className="flex items-center gap-4">
                                    <Tooltip content="Muted tags require high contrast ratio" placement="top">
                                        <Badge variant="cyan" glow>Tooltip Trigger</Badge>
                                    </Tooltip>
                                    <Badge variant="purple" glow>Accredited</Badge>
                                    <Badge variant="green" glow>Auto Match</Badge>
                                </div>
                            </div>
                            <div className="flex flex-col gap-6">
                                <ProgressBar value={68} />
                                <div className="flex flex-col gap-2.5">
                                    <Skeleton className="h-4 w-1/3" />
                                    <Skeleton className="h-3 w-full" />
                                    <Skeleton className="h-3 w-5/6" />
                                </div>
                            </div>
                        </div>
                    </Card>
                </ScrollReveal>

                {/* Empty State */}
                <ScrollReveal delay={0.6}>
                    <EmptyState
                        title="No recommendations imported"
                        description="You haven't added any college recommendations to your workflow shortlist yet. Get started by exploring matches."
                        action={<Button variant="primary">Calculate Matches <ArrowUpRight className="w-4 h-4 ml-1" /></Button>}
                    />
                </ScrollReveal>
            </div>

            {/* Dialog Overlay */}
            <Dialog isOpen={isDialogOpen} onClose={() => setIsDialogOpen(false)} title="System Credentials">
                <p className="mb-4">Enterprise access key validation is performed against Spring Security Method ACL. Ensure your role maps correctly.</p>
                <div className="flex justify-end gap-3">
                    <Button variant="secondary" onClick={() => setIsDialogOpen(false)}>Close</Button>
                    <Button variant="primary" onClick={() => setIsDialogOpen(false)}>Confirm</Button>
                </div>
            </Dialog>

            {/* Drawer Panel */}
            <Drawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)} title="Configuration Audit">
                <p className="mb-6">Cron schedules and cache eviction timers are monitored asynchronously. To evict dashboard stats manual caches, execute trigger requests.</p>
                <Button variant="danger" className="w-full" onClick={() => setIsDrawerOpen(false)}>Evict Caches</Button>
            </Drawer>
        </div>
    );
}
