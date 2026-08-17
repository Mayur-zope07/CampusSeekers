"use client";

import React, { useState } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { WelcomeHeader } from "@/components/dashboard/WelcomeHeader";
import { StatsSection } from "@/components/dashboard/StatsSection";
import { TasksPanel } from "@/components/dashboard/TasksPanel";
import { DashboardExport } from "@/components/dashboard/DashboardExport";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { useDashboardData, useProfile, useStudentScores } from "@/hooks/useDashboard";
import { PlusCircle } from "lucide-react";
import { useRouter } from "next/navigation";

export default function StudentDashboardPage() {
    const router = useRouter();
    const [sidebarItem, setSidebarItem] = useState("dashboard");

    // Queries to backend REST APIs
    const { data: dbData, isLoading: dbLoading, refetch } = useDashboardData();
    const { data: profile, isLoading: profileLoading } = useProfile();
    const { data: scores, isLoading: scoresLoading } = useStudentScores();

    const isLoading = dbLoading || profileLoading || scoresLoading;

    // Resolve current student details
    const studentName = profile ? { firstName: profile.firstName, lastName: profile.lastName } : { firstName: "Student", lastName: "" };
    const scoreItem = scores && scores.length > 0 ? scores[0] : { examName: "MHT_CET", percentile: 0 };

    return (
        <ProtectedRoute>
            <div className="min-h-screen bg-primary-bg text-white pl-0 md:pl-64 pt-24 pb-16 px-6 relative overflow-hidden">
                <Navbar />
                <Sidebar activeItem={sidebarItem} onChange={setSidebarItem} />

                {/* Ambient glow mesh */}
                <div className="absolute inset-0 opacity-[0.02] pointer-events-none z-0">
                    <div className="absolute top-[20%] left-[30%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px]" />
                    <div className="absolute bottom-[20%] right-[30%] w-96 h-96 bg-accent-purple rounded-full blur-[150px]" />
                </div>

                <div className="max-w-5xl mx-auto flex flex-col gap-8 relative z-10">
                    {/* Welcome Header loading state */}
                    {isLoading ? (
                        <div className="flex flex-col gap-2.5">
                            <Skeleton className="h-4 w-28" />
                            <Skeleton className="h-8 w-80" />
                            <Skeleton className="h-4 w-96" />
                        </div>
                    ) : (
                        <WelcomeHeader
                            firstName={studentName.firstName}
                            lastName={studentName.lastName}
                            percentile={Number(scoreItem.percentile)}
                            examName={scoreItem.examName}
                        />
                    )}

                    {/* Stats section */}
                    {isLoading ? (
                        <div className="grid grid-cols-2 md:grid-cols-7 gap-4">
                            {[1, 2, 3, 4, 5, 6, 7].map((x) => (
                                <Skeleton key={x} className="h-20 w-full" />
                            ))}
                        </div>
                    ) : (
                        <StatsSection
                            wishlistCount={dbData?.statistics?.wishlistCount}
                            shortlistCount={dbData?.statistics?.shortlistCount}
                            safeCount={dbData?.statistics?.safeCount}
                            targetCount={dbData?.statistics?.targetCount}
                            dreamCount={dbData?.statistics?.dreamCount}
                            averageFees={dbData?.statistics?.averageFees ? Number(dbData?.statistics?.averageFees) : 0}
                            highestPackage={dbData?.statistics?.highestPackage ? Number(dbData?.statistics?.highestPackage) : 0}
                        />
                    )}

                    {/* Main Workspace Widgets grid */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Column 1: Recommendations Snapshot */}
                        <div className="md:col-span-2 flex flex-col gap-6">
                            <Card className="flex flex-col gap-4 p-5 text-left h-full" hoverLift={false} glowColor="rgba(0, 240, 255, 0.05)">
                                <div className="flex justify-between items-center select-none border-b border-border-color/20 pb-3">
                                    <div className="flex flex-col gap-0.5">
                                        <Badge variant="cyan" className="w-fit py-0 px-1.5 text-[8.5px]">Recommendations</Badge>
                                        <h3 className="text-sm font-bold text-white mt-1">Smart Match Options</h3>
                                    </div>
                                    <Button variant="secondary" size="sm" onClick={() => refetch()}>
                                        Refresh Matches
                                    </Button>
                                </div>

                                {isLoading ? (
                                    <div className="flex flex-col gap-4">
                                        {[1, 2, 3].map((x) => (
                                            <Skeleton key={x} className="h-16 w-full" />
                                        ))}
                                    </div>
                                ) : dbData?.recentRecommendations && dbData.recentRecommendations.length > 0 ? (
                                    <div className="flex flex-col gap-3">
                                        {dbData.recentRecommendations.slice(0, 3).map((item: { matchCategory?: string; collegeName?: string; branchName?: string; percentileDifference?: number }, idx: number) => {
                                            const mapBadge = {
                                                SAFE: "green" as const,
                                                TARGET: "cyan" as const,
                                                DREAM: "purple" as const,
                                            }[item.matchCategory as "SAFE" | "TARGET" | "DREAM"] || "default";

                                            return (
                                                <div key={idx} className="glass-sm p-4 border border-border-color/40 rounded-sm flex justify-between items-center text-xs select-none">
                                                    <div className="flex flex-col gap-1">
                                                        <div className="flex items-center gap-2">
                                                            <Badge variant={mapBadge} className="py-0 px-1 text-[9px]">{item.matchCategory}</Badge>
                                                            <span className="font-semibold text-white">{item.collegeName}</span>
                                                        </div>
                                                        <span className="text-text-tertiary text-[10px]">{item.branchName}</span>
                                                    </div>
                                                    <div className="text-right shrink-0">
                                                        <span className="text-accent-cyan font-bold block">{(item.percentileDifference !== undefined && item.percentileDifference > 0) ? `+${item.percentileDifference}` : (item.percentileDifference ?? 0)}%</span>
                                                        <span className="text-text-tertiary text-[9px]">Difference</span>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                ) : (
                                    <EmptyState
                                        title="No matched options yet"
                                        description="Generate recommendations using your entrance scores."
                                        action={
                                            <Button variant="primary" size="sm" onClick={() => router.push("/app/onboarding")}>
                                                Setup Scores <PlusCircle className="w-3.5 h-3.5 ml-1" />
                                            </Button>
                                        }
                                    />
                                )}
                            </Card>
                        </div>

                        {/* Column 2: Tasks & Exports */}
                        <div className="flex flex-col gap-6">
                            <TasksPanel
                                hasProfile={!!profile}
                                hasScores={scores && scores.length > 0}
                                hasRecommendations={dbData?.recentRecommendations && dbData.recentRecommendations.length > 0}
                                hasWishlist={dbData?.recentWishlist && dbData.recentWishlist.length > 0}
                            />
                            <DashboardExport />
                        </div>
                    </div>

                    {/* Timeline & Trackers */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Wishlist Widget */}
                        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
                            <div className="flex flex-col gap-0.5 select-none border-b border-border-color/20 pb-3">
                                <Badge variant="purple" className="w-fit py-0 px-1.5 text-[8.5px]">Wishlist</Badge>
                                <h3 className="text-sm font-bold text-white mt-1">Recent Saved Colleges</h3>
                            </div>

                            {isLoading ? (
                                <div className="flex flex-col gap-2.5">
                                    <Skeleton className="h-10 w-full" />
                                    <Skeleton className="h-10 w-full" />
                                </div>
                            ) : dbData?.recentWishlist && dbData.recentWishlist.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {dbData.recentWishlist.slice(0, 3).map((item: { collegeName?: string; homeState?: string }, idx: number) => (
                                        <div key={idx} className="glass-sm p-3 border border-border-color/30 rounded-sm flex justify-between items-center text-xs">
                                            <span className="font-semibold text-white">{item.collegeName}</span>
                                            <span className="text-[10px] text-text-tertiary">{item.homeState}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-6 text-xs text-text-tertiary select-none">
                                    No colleges in wishlist.
                                </div>
                            )}
                        </Card>

                        {/* Shortlists Widget */}
                        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
                            <div className="flex flex-col gap-0.5 select-none border-b border-border-color/20 pb-3">
                                <Badge variant="cyan" className="w-fit py-0 px-1.5 text-[8.5px]">Shortlist</Badge>
                                <h3 className="text-sm font-bold text-white mt-1">Shortlisted Preferences</h3>
                            </div>

                            {isLoading ? (
                                <div className="flex flex-col gap-2.5">
                                    <Skeleton className="h-10 w-full" />
                                    <Skeleton className="h-10 w-full" />
                                </div>
                            ) : dbData?.recentShortlists && dbData.recentShortlists.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {dbData.recentShortlists.slice(0, 3).map((item: { collegeName?: string; branchName?: string; priority?: number }, idx: number) => (
                                        <div key={idx} className="glass-sm p-3 border border-border-color/30 rounded-sm flex justify-between items-center text-xs">
                                            <div className="flex flex-col gap-0.5">
                                                <span className="font-semibold text-white">{item.collegeName}</span>
                                                <span className="text-[10px] text-text-tertiary">{item.branchName}</span>
                                            </div>
                                            <Badge variant="cyan" className="py-0 px-1.5">Pri {item.priority}</Badge>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-6 text-xs text-text-tertiary select-none">
                                    No branches shortlisted.
                                </div>
                            )}
                        </Card>

                        {/* Admission activity Timeline Widget */}
                        <Card className="flex flex-col gap-4 p-5 text-left" hoverLift={false}>
                            <div className="flex flex-col gap-0.5 select-none border-b border-border-color/20 pb-3">
                                <Badge variant="purple" className="w-fit py-0 px-1.5 text-[8.5px]">Timeline</Badge>
                                <h3 className="text-sm font-bold text-white mt-1">Recent Activity</h3>
                            </div>

                            {isLoading ? (
                                <div className="flex flex-col gap-2.5">
                                    <Skeleton className="h-10 w-full" />
                                    <Skeleton className="h-10 w-full" />
                                </div>
                            ) : dbData?.recentAdmissionActivity && dbData.recentAdmissionActivity.length > 0 ? (
                                <div className="flex flex-col gap-3">
                                    {dbData.recentAdmissionActivity.slice(0, 3).map((item: { remarks?: string; status?: string }, idx: number) => (
                                        <div key={idx} className="glass-sm p-3 border border-border-color/30 rounded-sm flex flex-col gap-1 text-xs text-left">
                                            <span className="text-white font-semibold">{item.remarks}</span>
                                            <span className="text-[10px] text-text-tertiary">Status: {item.status}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-6 text-xs text-text-tertiary select-none">
                                    No recent workflow activities.
                                </div>
                            )}
                        </Card>
                    </div>
                </div>

            </div>
        </ProtectedRoute>
    );
}
