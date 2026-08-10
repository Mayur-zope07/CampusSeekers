"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { GraduationCap, Mail, ArrowRight, ArrowLeft, ShieldCheck } from "lucide-react";

export default function ForgotPasswordPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!email) return;
        setIsLoading(true);
        setTimeout(() => {
            setIsLoading(false);
            setIsSuccess(true);
        }, 1500);
    };

    return (
        <main className="min-h-screen bg-primary-bg flex items-center justify-center p-6 relative overflow-hidden">
            <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0">
                <div className="absolute top-[10%] left-[20%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px]" />
            </div>

            <div className="w-full max-w-md relative z-10">
                <ScrollReveal>
                    <Card className="flex flex-col gap-6" glowColor="rgba(0, 240, 255, 0.12)">
                        <div className="flex flex-col gap-1.5 items-center text-center select-none">
                            <GraduationCap className="w-8 h-8 text-white animate-pulse" />
                            <Badge variant="cyan" glow className="mt-2">Credentials Recovery</Badge>
                            <h2 className="text-xl font-bold text-white tracking-wide mt-1">Forgot Password?</h2>
                            <p className="text-xs text-text-secondary">Enter your email and we&apos;ll dispatch reset credentials instructions.</p>
                        </div>

                        {isSuccess ? (
                            <div className="flex flex-col gap-4 text-center items-center py-4">
                                <div className="w-10 h-10 rounded-full border border-accent-green/20 bg-accent-green/5 flex items-center justify-center text-accent-green shrink-0">
                                    <ShieldCheck className="w-5 h-5" />
                                </div>
                                <div className="flex flex-col gap-1">
                                    <h4 className="text-sm font-bold text-white">Instructions Dispatched</h4>
                                    <p className="text-xs text-text-secondary leading-relaxed px-4">
                                        Check your email box. We have sent credentials recovery instructions to <span className="text-white font-semibold">{email}</span>.
                                    </p>
                                </div>
                                <Button variant="secondary" size="sm" className="mt-2" onClick={() => router.push("/app/login")}>
                                    <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Sign In
                                </Button>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                                <Input
                                    label="Email Address"
                                    type="email"
                                    placeholder="name@example.com"
                                    icon={<Mail className="w-4 h-4" />}
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />

                                <Button variant="primary" size="lg" className="w-full mt-2" isLoading={isLoading} type="submit">
                                    Recover Password <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                </Button>

                                <button
                                    type="button"
                                    onClick={() => router.push("/app/login")}
                                    className="flex items-center justify-center gap-1.5 text-xs text-text-secondary hover:text-white transition-colors cursor-pointer select-none"
                                >
                                    <ArrowLeft className="w-4 h-4" />
                                    <span>Back to Sign In</span>
                                </button>
                            </form>
                        )}
                    </Card>
                </ScrollReveal>
            </div>
        </main>
    );
}
