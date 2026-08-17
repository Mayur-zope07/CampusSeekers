"use client";

import React, { useState } from "react";
import { ProfileLayout } from "@/app/app/profile/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { useCurrentUser, useLogout } from "@/hooks/useProfile";
import {
    Shield, LogOut, Monitor, Clock, AlertCircle, CheckCircle, Lock,
} from "lucide-react";

export default function SecurityPage() {
    const { data: user, isLoading } = useCurrentUser();
    const { mutate: logout, isPending: loggingOut } = useLogout();
    const [confirmed, setConfirmed] = useState(false);

    const joinDate = user?.createdAt
        ? new Date(user.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" })
        : "—";

    return (
        <ProfileLayout activeItem="profile">
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left">
                        <Badge variant="green" className="w-fit text-[9px] px-2.5 py-1">
                            <Shield className="w-3 h-3 mr-1.5 inline" /> Security
                        </Badge>
                        <h1 className="text-2xl md:text-3xl font-extrabold text-white">Security Center</h1>
                        <p className="text-xs text-text-secondary max-w-md">Review your account security status and manage your session.</p>
                    </div>
                </ScrollReveal>

                {/* Account Info */}
                <ScrollReveal delay={0.05}>
                    <Card className="flex flex-col gap-5 p-6 text-left" hoverLift={false}>
                        <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Account Information</span>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            {[
                                { label: "Email Address", value: user?.email ?? "—", icon: <CheckCircle className="w-3.5 h-3.5 text-accent-green" /> },
                                { label: "Account Role",  value: user?.role  ?? "STUDENT", icon: <Shield className="w-3.5 h-3.5 text-accent-cyan" /> },
                                { label: "Member Since",  value: joinDate,                 icon: <Clock className="w-3.5 h-3.5 text-text-secondary" /> },
                                { label: "Account ID",    value: user?.id ? user.id.slice(0, 16) + "…" : "—", icon: <Monitor className="w-3.5 h-3.5 text-text-secondary" /> },
                            ].map(item => (
                                <div key={item.label} className="flex items-center gap-3 p-3 glass-sm border border-border-color/20 rounded-xs">
                                    {item.icon}
                                    <div className="flex flex-col gap-0.5 min-w-0">
                                        <span className="text-[9px] text-text-tertiary uppercase tracking-wider">{item.label}</span>
                                        <span className="text-[11px] text-white font-semibold truncate">{isLoading ? "Loading…" : item.value}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </Card>
                </ScrollReveal>

                {/* Password Change Note */}
                <ScrollReveal delay={0.08}>
                    <Card className="flex flex-col gap-4 p-6 text-left border border-border-color/20" hoverLift={false}>
                        <div className="flex items-center gap-3">
                            <Lock className="w-5 h-5 text-accent-orange shrink-0" />
                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Password Management</span>
                        </div>
                        <div className="flex items-start gap-3 px-4 py-3 rounded-xs border border-accent-orange/20 bg-accent-orange/5">
                            <AlertCircle className="w-4 h-4 text-accent-orange shrink-0 mt-0.5" />
                            <p className="text-[11px] text-text-secondary leading-relaxed">
                                Password change functionality requires a verified email flow that is planned for a future backend release. For now, contact support if you need to reset your password.
                            </p>
                        </div>
                    </Card>
                </ScrollReveal>

                {/* Logout */}
                <ScrollReveal delay={0.11}>
                    <Card className="flex flex-col gap-4 p-6 text-left border border-accent-orange/10" hoverLift={false}>
                        <div className="flex items-center gap-3">
                            <LogOut className="w-5 h-5 text-accent-orange shrink-0" />
                            <div className="flex flex-col gap-0.5">
                                <span className="text-sm font-bold text-white">Sign Out</span>
                                <span className="text-[11px] text-text-secondary">End your current session and return to the login page.</span>
                            </div>
                        </div>
                        {!confirmed ? (
                            <Button variant="secondary" size="sm" className="w-fit gap-1.5 text-accent-orange hover:border-accent-orange/40" onClick={() => setConfirmed(true)}>
                                <LogOut className="w-3.5 h-3.5" /> Sign Out
                            </Button>
                        ) : (
                            <div className="flex items-center gap-3">
                                <p className="text-[11px] text-text-secondary">Are you sure?</p>
                                <Button variant="primary" size="sm" isLoading={loggingOut} onClick={() => logout()} className="gap-1.5 bg-accent-orange/80 border-accent-orange/40">
                                    Confirm Sign Out
                                </Button>
                                <Button variant="secondary" size="sm" onClick={() => setConfirmed(false)}>Cancel</Button>
                            </div>
                        )}
                    </Card>
                </ScrollReveal>
            </div>
        </ProfileLayout>
    );
}
