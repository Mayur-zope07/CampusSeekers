"use client";

import React, { useState } from "react";
import { WorkspaceLayout } from "@/app/app/workspace/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import {
    useWishlist,
    useDeleteWishlist,
    useRestoreWishlist,
    type WishlistItem,
} from "@/hooks/useWorkflow";
import {
    Heart,
    MapPin,
    Trash2,
    RotateCcw,
    ArrowRight,
    Search,
    Building2,
} from "lucide-react";
import { useRouter } from "next/navigation";

// ─── Wishlist Card ────────────────────────────────────────────────────────────

function WishlistCard({
    item,
    onDelete,
    onRestore,
    onView,
    isDeleting,
    isRestoring,
}: {
    item: WishlistItem;
    onDelete: (id: string) => void;
    onRestore: (id: string) => void;
    onView: (id: string) => void;
    isDeleting: boolean;
    isRestoring: boolean;
}) {
    const date = new Date(item.createdAt).toLocaleDateString("en-IN", {
        day: "numeric", month: "short", year: "numeric",
    });
    return (
        <Card className={`flex flex-col gap-4 p-5 text-left transition-opacity duration-300 ${item.isDeleted ? "opacity-50" : ""}`} hoverLift={!item.isDeleted}>
            {/* Header */}
            <div className="flex items-start justify-between gap-3">
                <div className="flex flex-col gap-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                        {item.isDeleted && <Badge variant="default" className="text-[8.5px] px-1.5 py-0">Deleted</Badge>}
                        {item.naacGrade && <Badge variant="cyan" className="text-[8.5px] px-1.5 py-0">{item.naacGrade}</Badge>}
                    </div>
                    <h3 className="font-bold text-sm text-white mt-1">{item.collegeName}</h3>
                    <div className="flex items-center gap-1.5 text-[11px] text-text-tertiary">
                        <MapPin className="w-3 h-3" />
                        {item.city}, {item.state}
                    </div>
                </div>
                <div className="w-9 h-9 rounded-xs border border-border-color/20 bg-accent-orange/5 flex items-center justify-center shrink-0">
                    <Heart className="w-4 h-4 text-accent-orange" />
                </div>
            </div>

            <div className="text-[10px] text-text-tertiary border-t border-border-color/20 pt-3">
                Added {date} · Code: {item.collegeCode}
            </div>

            {/* Actions */}
            <div className="flex gap-2">
                <Button variant="secondary" size="sm" className="flex-1 gap-1.5" onClick={() => onView(item.collegeId)}>
                    <ArrowRight className="w-3.5 h-3.5" /> View
                </Button>
                {item.isDeleted ? (
                    <Button variant="primary" size="sm" className="flex-1 gap-1.5" isLoading={isRestoring} onClick={() => onRestore(item.id)}>
                        <RotateCcw className="w-3.5 h-3.5" /> Restore
                    </Button>
                ) : (
                    <Button variant="secondary" size="sm" className="flex-1 gap-1.5 text-accent-orange hover:border-accent-orange/40" isLoading={isDeleting} onClick={() => onDelete(item.id)}>
                        <Trash2 className="w-3.5 h-3.5" /> Remove
                    </Button>
                )}
            </div>
        </Card>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function WishlistPage() {
    const router = useRouter();
    const toast = useToast();
    const [keyword, setKeyword] = useState("");
    const [naac, setNaac] = useState("");
    const [deletingId, setDeletingId] = useState<string | null>(null);
    const [restoringId, setRestoringId] = useState<string | null>(null);

    const { data: wishlist, isLoading } = useWishlist({ keyword: keyword || undefined, naac: naac || undefined });
    const { mutate: deleteWishlist } = useDeleteWishlist();
    const { mutate: restoreWishlist } = useRestoreWishlist();

    const handleDelete = (id: string) => {
        setDeletingId(id);
        deleteWishlist(id, {
            onSuccess: () => { toast.success("Removed from wishlist."); setDeletingId(null); },
            onError: () => { toast.error("Failed to remove."); setDeletingId(null); },
        });
    };

    const handleRestore = (id: string) => {
        setRestoringId(id);
        restoreWishlist(id, {
            onSuccess: () => { toast.success("Wishlist entry restored!"); setRestoringId(null); },
            onError: () => { toast.error("Failed to restore."); setRestoringId(null); },
        });
    };

    return (
        <WorkspaceLayout>
            <div className="flex flex-col gap-8">
                {/* Header */}
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left select-none">
                        <Badge variant="orange" className="w-fit text-[9px] px-2.5 py-1">
                            <Heart className="w-3 h-3 mr-1.5 inline" /> Wishlist
                        </Badge>
                        <h1 className="text-2xl md:text-4xl font-extrabold text-white tracking-tight">
                            Saved{" "}
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-orange to-accent-yellow">
                                Colleges
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary max-w-md leading-relaxed">
                            Your personal collection of favourite colleges. Soft-deleted entries can be restored any time.
                        </p>
                    </div>
                </ScrollReveal>

                {/* Search + Filter */}
                <ScrollReveal delay={0.05}>
                    <div className="flex flex-col sm:flex-row gap-3">
                        <div className="relative flex-1">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-tertiary pointer-events-none" />
                            <input
                                value={keyword}
                                onChange={e => setKeyword(e.target.value)}
                                placeholder="Search by college name or city…"
                                className="w-full pl-9 pr-4 py-2.5 glass-sm border border-border-color/40 rounded-xs text-[11px] text-white placeholder:text-text-tertiary focus:outline-none focus:border-accent-cyan/50 transition-colors bg-transparent"
                            />
                        </div>
                        <Input label="" placeholder="Min NAAC grade (e.g. A+)" value={naac} onChange={e => setNaac(e.target.value)} className="sm:w-48" />
                    </div>
                </ScrollReveal>

                {/* Grid */}
                {isLoading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                        {[1, 2, 3, 4, 5, 6].map(x => <Skeleton key={x} className="h-44" />)}
                    </div>
                ) : wishlist && wishlist.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                        {wishlist.map((item, i) => (
                            <ScrollReveal key={item.id} delay={i * 0.04}>
                                <WishlistCard
                                    item={item}
                                    onDelete={handleDelete}
                                    onRestore={handleRestore}
                                    onView={id => router.push(`/app/colleges/${id}`)}
                                    isDeleting={deletingId === item.id}
                                    isRestoring={restoringId === item.id}
                                />
                            </ScrollReveal>
                        ))}
                    </div>
                ) : (
                    <EmptyState
                        title="Your wishlist is empty"
                        description="Start saving colleges from the search or college detail pages to build your personal collection."
                        action={
                            <Button variant="primary" size="sm" onClick={() => router.push("/app/search")}>
                                <Building2 className="w-3.5 h-3.5 mr-1.5" /> Explore Colleges
                            </Button>
                        }
                    />
                )}
            </div>
        </WorkspaceLayout>
    );
}
