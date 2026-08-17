"use client";

import React, { useState } from "react";
import { ProfileLayout } from "@/app/app/profile/page";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { Settings, Monitor, Bell, Palette } from "lucide-react";

// ─── Toggle Switch ─────────────────────────────────────────────────────────────

function Toggle({ enabled, onChange }: { enabled: boolean; onChange: (v: boolean) => void }) {
    return (
        <button onClick={() => onChange(!enabled)} role="switch" aria-checked={enabled}
            className={`relative w-10 h-5.5 rounded-full border transition-all cursor-pointer shrink-0 ${enabled ? "bg-accent-cyan/20 border-accent-cyan/40" : "bg-white/5 border-border-color/40"}`}>
            <span className={`absolute top-0.5 w-4 h-4 rounded-full transition-all shadow ${enabled ? "left-[22px] bg-accent-cyan" : "left-0.5 bg-text-tertiary"}`} />
        </button>
    );
}

// ─── Setting Row ──────────────────────────────────────────────────────────────

function SettingRow({ label, desc, enabled, onChange }: { label: string; desc: string; enabled: boolean; onChange: (v: boolean) => void }) {
    return (
        <div className="flex items-center justify-between gap-4 py-3 border-b border-border-color/15 last:border-b-0">
            <div className="flex flex-col gap-0.5 min-w-0">
                <span className="text-[11px] font-semibold text-white">{label}</span>
                <span className="text-[10px] text-text-tertiary leading-relaxed">{desc}</span>
            </div>
            <Toggle enabled={enabled} onChange={onChange} />
        </div>
    );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function AppSettingsPage() {
    const [notifications, setNotifications] = useState({
        recommendations: true,
        wishlist:         false,
        tracker:          true,
        announcements:    false,
    });

    const [appearance, setAppearance] = useState({
        reducedMotion:  false,
        compactMode:    false,
        showAnimations: true,
    });

    const [display, setDisplay] = useState({
        showCompletionRing: true,
        showActivityFeed:   true,
    });

    return (
        <ProfileLayout activeItem="profile">
            <div className="flex flex-col gap-8">
                <ScrollReveal>
                    <div className="flex flex-col gap-2 text-left">
                        <Badge variant="default" className="w-fit text-[9px] px-2.5 py-1">
                            <Settings className="w-3 h-3 mr-1.5 inline" /> App Settings
                        </Badge>
                        <h1 className="text-2xl md:text-3xl font-extrabold text-white">Application Preferences</h1>
                        <p className="text-xs text-text-secondary max-w-md">Control how CampusSeekers behaves, looks, and notifies you.</p>
                    </div>
                </ScrollReveal>

                {/* Notification Preferences */}
                <ScrollReveal delay={0.05}>
                    <Card className="flex flex-col gap-4 p-6 text-left" hoverLift={false}>
                        <div className="flex items-center gap-2.5">
                            <Bell className="w-4 h-4 text-accent-cyan" />
                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Notification Preferences</span>
                        </div>
                        <div className="flex flex-col">
                            <SettingRow label="Recommendation Alerts" desc="Notify when new AI recommendations are available." enabled={notifications.recommendations} onChange={v => setNotifications(p => ({ ...p, recommendations: v }))} />
                            <SettingRow label="Wishlist Updates" desc="Notify when a wishlisted college updates its information." enabled={notifications.wishlist} onChange={v => setNotifications(p => ({ ...p, wishlist: v }))} />
                            <SettingRow label="Tracker Updates" desc="Notify when admission tracker statuses change." enabled={notifications.tracker} onChange={v => setNotifications(p => ({ ...p, tracker: v }))} />
                            <SettingRow label="Platform Announcements" desc="Receive major platform news and feature updates." enabled={notifications.announcements} onChange={v => setNotifications(p => ({ ...p, announcements: v }))} />
                        </div>
                    </Card>
                </ScrollReveal>

                {/* Appearance */}
                <ScrollReveal delay={0.08}>
                    <Card className="flex flex-col gap-4 p-6 text-left" hoverLift={false}>
                        <div className="flex items-center gap-2.5">
                            <Palette className="w-4 h-4 text-accent-purple" />
                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Appearance</span>
                        </div>
                        <div className="flex flex-col">
                            <SettingRow label="Show Animations" desc="Enable micro-animations and transition effects across the platform." enabled={appearance.showAnimations} onChange={v => setAppearance(p => ({ ...p, showAnimations: v }))} />
                            <SettingRow label="Reduced Motion" desc="Minimize motion for accessibility. Overrides animation settings." enabled={appearance.reducedMotion} onChange={v => setAppearance(p => ({ ...p, reducedMotion: v }))} />
                            <SettingRow label="Compact Mode" desc="Reduce padding and spacing for a denser layout." enabled={appearance.compactMode} onChange={v => setAppearance(p => ({ ...p, compactMode: v }))} />
                        </div>
                        <div className="flex gap-3 flex-wrap pt-2 border-t border-border-color/20">
                            {["System", "Dark", "Light"].map(mode => (
                                <button key={mode}
                                    className={`px-3 py-2 text-[10px] font-bold rounded-xs border transition-all cursor-pointer select-none ${mode === "Dark" ? "border-accent-cyan/40 text-accent-cyan bg-accent-cyan/8" : "border-border-color/30 text-text-secondary hover:text-white"}`}>
                                    {mode === "System" && <Monitor className="w-3 h-3 inline mr-1.5" />}
                                    {mode}
                                </button>
                            ))}
                        </div>
                    </Card>
                </ScrollReveal>

                {/* Display */}
                <ScrollReveal delay={0.11}>
                    <Card className="flex flex-col gap-4 p-6 text-left" hoverLift={false}>
                        <div className="flex items-center gap-2.5">
                            <Monitor className="w-4 h-4 text-accent-green" />
                            <span className="text-[10px] font-bold uppercase tracking-wider text-text-tertiary">Display Options</span>
                        </div>
                        <div className="flex flex-col">
                            <SettingRow label="Show Completion Ring" desc="Display the profile completion ring on the overview page." enabled={display.showCompletionRing} onChange={v => setDisplay(p => ({ ...p, showCompletionRing: v }))} />
                            <SettingRow label="Show Activity Feed" desc="Show recent activity timeline on dashboard and profile." enabled={display.showActivityFeed} onChange={v => setDisplay(p => ({ ...p, showActivityFeed: v }))} />
                        </div>
                    </Card>
                </ScrollReveal>
            </div>
        </ProfileLayout>
    );
}
