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
import { useCollege } from "@/hooks/useCollegeSearch";
import { useToast } from "@/providers/ToastProvider";
import { useCreateWishlist } from "@/hooks/useWorkflow";
import { GraduationCap, ArrowLeft, PlusCircle, IndianRupee, TrendingUp, Building2, MapPin, Heart } from "lucide-react";

interface CollegeDetailsProps {
    params: Promise<{ id: string }>;
}

export default function CollegeDetailsPage({ params }: CollegeDetailsProps) {
    const router = useRouter();
    const toast = useToast();
    const { id } = use(params);

    const { data: college, isLoading } = useCollege(id);
    const { mutate: createWishlist, isPending: wishlistPending } = useCreateWishlist();

    const handleWishlist = () => {
        if (!college) return;
        createWishlist(id, {
            onSuccess: () => toast.success(`${college.name} added to wishlist!`),
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
                <Sidebar activeItem="search" onChange={() => {}} />

                <div className="max-w-4xl mx-auto flex flex-col gap-8 relative z-10">
                    <button
                        onClick={() => router.push("/app/search")}
                        className="flex items-center gap-2 text-xs text-text-secondary hover:text-white transition-colors cursor-pointer select-none w-fit"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        <span>Back to Discovery</span>
                    </button>

                    {isLoading ? (
                        <div className="flex flex-col gap-4 text-left">
                            <Skeleton className="h-10 w-2/3" />
                            <Skeleton className="h-4 w-1/2" />
                            <Skeleton className="h-48 w-full" />
                        </div>
                    ) : college ? (
                        <div className="flex flex-col gap-8 text-left">
                            <ScrollReveal>
                                <div className="flex flex-col gap-3">
                                    <div className="flex flex-wrap items-center gap-2">
                                        <Badge variant="purple" glow>NAAC {college.naacGrade || "A"}</Badge>
                                        <Badge variant="cyan">{college.type || "Government"}</Badge>
                                        {college.nba && <Badge variant="green">NBA Accredited</Badge>}
                                    </div>
                                    <h1 className="text-3xl md:text-5xl font-extrabold text-white tracking-tight animate-fade-in">
                                        {college.name}
                                    </h1>
                                    <div className="flex flex-wrap gap-4 text-xs text-text-secondary mt-1">
                                        <span className="flex items-center gap-1.5"><MapPin className="w-3.5 h-3.5" /> {college.city}, {college.state}</span>
                                        <span className="flex items-center gap-1.5"><Building2 className="w-3.5 h-3.5" /> Code: {college.code || "1002"}</span>
                                    </div>
                                </div>
                            </ScrollReveal>

                            <ScrollReveal delay={0.1}>
                                <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 select-none">
                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false} glowColor="rgba(57, 255, 20, 0.05)">
                                        <div className="flex items-center justify-between text-text-secondary">
                                            <span className="text-[10px] font-bold uppercase tracking-wider">Average Package</span>
                                            <IndianRupee className="w-4 h-4" />
                                        </div>
                                        <span className="text-2xl font-light font-futuristic text-white mt-2">
                                            ₹{college.averagePackage ? (college.averagePackage / 100000).toFixed(1) : "6.5"} L
                                        </span>
                                    </Card>

                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false} glowColor="rgba(0, 240, 255, 0.05)">
                                        <div className="flex items-center justify-between text-text-secondary">
                                            <span className="text-[10px] font-bold uppercase tracking-wider">Highest Package</span>
                                            <TrendingUp className="w-4 h-4 text-accent-green" />
                                        </div>
                                        <span className="text-2xl font-light font-futuristic text-white mt-2">
                                            ₹{college.highestPackage ? (college.highestPackage / 100000).toFixed(1) : "12.0"} L
                                        </span>
                                    </Card>

                                    <Card className="flex flex-col gap-2 p-5" hoverLift={false}>
                                        <div className="flex items-center justify-between text-text-secondary">
                                            <span className="text-[10px] font-bold uppercase tracking-wider">Placements Ratio</span>
                                            <GraduationCap className="w-4 h-4" />
                                        </div>
                                        <span className="text-2xl font-light font-futuristic text-white mt-2">
                                            {college.placementRatio || "85"}%
                                        </span>
                                    </Card>
                                </div>
                            </ScrollReveal>

                            <ScrollReveal delay={0.15}>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <Card className="flex flex-col gap-4 p-5 h-full" hoverLift={false}>
                                        <h3 className="text-sm font-bold text-white uppercase tracking-wider select-none">Branches & Programs</h3>
                                        <div className="flex flex-col gap-2 text-xs">
                                            {college.branches && college.branches.length > 0 ? (
                                                college.branches.map((b: string, idx: number) => (
                                                    <div key={idx} className="glass-sm p-3 border border-border-color/30 rounded-sm">
                                                        {b}
                                                    </div>
                                                ))
                                            ) : (
                                                ["Computer Engineering", "Information Technology", "Electronics Engineering", "Mechanical Engineering"].map((b, idx) => (
                                                    <div key={idx} className="glass-sm p-3 border border-border-color/30 rounded-sm">
                                                        {b}
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    </Card>

                                    <Card className="flex flex-col gap-4 p-5 h-full" hoverLift={false}>
                                        <h3 className="text-sm font-bold text-white uppercase tracking-wider select-none">Facilities</h3>
                                        <div className="flex flex-wrap gap-2 text-xs">
                                            {["Digital Library", "Boys Hostel", "Girls Hostel", "Gymnasium", "Placement Cell", "Research Lab", "WiFi Campus"].map((f, idx) => (
                                                <Badge key={idx} variant="default" className="py-1.5 px-3">
                                                    {f}
                                                </Badge>
                                            ))}
                                        </div>
                                    </Card>
                                </div>
                            </ScrollReveal>

                            <ScrollReveal delay={0.2}>
                                <div className="flex gap-4 border-t border-border-color/30 pt-6">
                                    <Button variant="primary" size="lg" onClick={() => router.push("/app/recommendations")}>
                                        Get AI Matches <PlusCircle className="w-4 h-4 ml-1.5 shrink-0" />
                                    </Button>
                                    <Button variant="secondary" size="lg" isLoading={wishlistPending} onClick={handleWishlist}>
                                        <Heart className="w-4 h-4 mr-1.5" /> Wishlist
                                    </Button>
                                </div>
                            </ScrollReveal>
                        </div>
                    ) : (
                        <div className="py-20">
                            <EmptyState
                                title="Institution profile not found"
                                description="The requested college profile does not exist in the state database."
                            />
                        </div>
                    )}
                </div>
            </div>
        </ProtectedRoute>
    );
}
