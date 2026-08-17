"use client";

import React, { useState, useEffect } from "react";
import { ProfileLayout } from "@/app/app/profile/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Dropdown } from "@/components/ui/Dropdown";
import { Skeleton } from "@/components/ui/Skeleton";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useToast } from "@/providers/ToastProvider";
import { useProfile, useUpdateProfile, type StudentProfilePayload, type Gender, type Category } from "@/hooks/useProfile";
import { User, Save } from "lucide-react";

const GENDERS: { label: string; value: Gender }[] = [
    { label: "Male", value: "MALE" },
    { label: "Female", value: "FEMALE" },
    { label: "Other", value: "OTHER" },
];

const CATEGORIES: { label: string; value: Category }[] = [
    { label: "OPEN", value: "OPEN" }, { label: "OBC", value: "OBC" },
    { label: "SC", value: "SC" }, { label: "ST", value: "ST" },
    { label: "EWS", value: "EWS" }, { label: "NT1", value: "NT1" },
    { label: "NT2", value: "NT2" }, { label: "NT3", value: "NT3" },
];

export default function AcademicProfilePage() {
    const toast = useToast();
    const { data: profile, isLoading } = useProfile();
    const { mutate: updateProfile, isPending } = useUpdateProfile();

    const [form, setForm] = useState<StudentProfilePayload>({
        firstName: "", lastName: "", phone: "",
        gender: "MALE", category: "OPEN",
        subCategory: "", homeState: "", homeDistrict: "",
    });
    const [dirty, setDirty] = useState(false);

    useEffect(() => {
        if (profile) {
            setForm({
                firstName:   profile.firstName,
                lastName:    profile.lastName,
                phone:       profile.phone,
                gender:      profile.gender,
                category:    profile.category,
                subCategory: profile.subCategory ?? "",
                homeState:   profile.homeState,
                homeDistrict: profile.homeDistrict,
            });
        }
    }, [profile]);

    const update = (fields: Partial<StudentProfilePayload>) => {
        setForm(p => ({ ...p, ...fields }));
        setDirty(true);
    };

    const handleSave = () => {
        updateProfile(form, {
            onSuccess: () => { toast.success("Profile updated successfully!"); setDirty(false); },
            onError: (err: unknown) => {
                const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
                toast.error(msg ?? "Failed to update profile.");
            },
        });
    };

    return (
        <ProfileLayout activeItem="profile">
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex items-center justify-between gap-4">
                        <div className="flex flex-col gap-2">
                            <Badge variant="cyan" className="w-fit text-[9px] px-2.5 py-1">
                                <User className="w-3 h-3 mr-1.5 inline" /> Academic Profile
                            </Badge>
                            <h1 className="text-2xl md:text-3xl font-extrabold text-white">Personal Details</h1>
                            <p className="text-xs text-text-secondary max-w-md">Your name, contact, category and home details used across the platform.</p>
                        </div>
                        {dirty && (
                            <Button variant="primary" size="sm" isLoading={isPending} onClick={handleSave} className="gap-1.5 shrink-0">
                                <Save className="w-3.5 h-3.5" /> Save Changes
                            </Button>
                        )}
                    </div>
                </ScrollReveal>

                {isLoading ? (
                    <div className="flex flex-col gap-4">{[1, 2, 3, 4].map(x => <Skeleton key={x} className="h-14 w-full" />)}</div>
                ) : (
                    <div className="flex flex-col gap-5">
                        <ScrollReveal delay={0.05}>
                            <Card className="flex flex-col gap-5 p-6 text-left" hoverLift={false} allowOverflow={true}>
                                <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Personal Information</span>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <Input label="First Name *" value={form.firstName} onChange={e => update({ firstName: e.target.value })} placeholder="e.g. Arjun" required />
                                    <Input label="Last Name *" value={form.lastName} onChange={e => update({ lastName: e.target.value })} placeholder="e.g. Sharma" required />
                                    <Input label="Phone Number *" type="tel" value={form.phone} onChange={e => update({ phone: e.target.value })} placeholder="10-digit number" required />
                                    <Dropdown
                                        options={GENDERS.map(g => ({ label: g.label, value: g.value }))}
                                        selected={form.gender}
                                        onChange={v => update({ gender: v as Gender })}
                                    />
                                </div>
                            </Card>
                        </ScrollReveal>

                        <ScrollReveal delay={0.08}>
                            <Card className="flex flex-col gap-5 p-6 text-left" hoverLift={false} allowOverflow={true}>
                                <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Reservation Category</span>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <Dropdown
                                        options={CATEGORIES.map(c => ({ label: c.label, value: c.value }))}
                                        selected={form.category}
                                        onChange={v => update({ category: v as Category })}
                                    />
                                    <Input label="Sub-category (optional)" value={form.subCategory ?? ""} onChange={e => update({ subCategory: e.target.value })} placeholder="e.g. NT-B" />
                                </div>
                            </Card>
                        </ScrollReveal>

                        <ScrollReveal delay={0.1}>
                            <Card className="flex flex-col gap-5 p-6 text-left" hoverLift={false}>
                                <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Home Location</span>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <Input label="Home State *" value={form.homeState} onChange={e => update({ homeState: e.target.value })} placeholder="e.g. Maharashtra" required />
                                    <Input label="Home District *" value={form.homeDistrict} onChange={e => update({ homeDistrict: e.target.value })} placeholder="e.g. Pune" required />
                                </div>
                            </Card>
                        </ScrollReveal>

                        <ScrollReveal delay={0.12}>
                            <div className="flex justify-end">
                                <Button variant="primary" size="md" isLoading={isPending} disabled={!dirty} onClick={handleSave} className="gap-2">
                                    <Save className="w-4 h-4" /> Save Profile
                                </Button>
                            </div>
                        </ScrollReveal>
                    </div>
                )}
            </div>
        </ProfileLayout>
    );
}
