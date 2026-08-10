"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { PasswordStrength } from "@/components/ui/PasswordStrength";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { GraduationCap, Lock, ArrowRight, ShieldCheck } from "lucide-react";

export default function ResetPasswordPage() {
    const router = useRouter();
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        if (password.length < 6) {
            setError("Password must be at least 6 characters");
            return;
        }
        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        setIsLoading(true);
        setTimeout(() => {
            setIsLoading(false);
            setIsSuccess(true);
            setTimeout(() => {
                router.push("/app/login");
            }, 2500);
        }, 1500);
    };

    return (
        <main className="min-h-screen bg-primary-bg flex items-center justify-center p-6 relative overflow-hidden">
            <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0">
                <div className="absolute top-[10%] left-[20%] w-96 h-96 bg-accent-purple rounded-full blur-[150px]" />
            </div>

            <div className="w-full max-w-md relative z-10">
                <ScrollReveal>
                    <Card className="flex flex-col gap-6" glowColor="rgba(138, 43, 226, 0.12)">
                        <div className="flex flex-col gap-1.5 items-center text-center select-none">
                            <GraduationCap className="w-8 h-8 text-white animate-pulse" />
                            <Badge variant="purple" glow className="mt-2">Credentials Recovery</Badge>
                            <h2 className="text-xl font-bold text-white tracking-wide mt-1">Reset Password</h2>
                            <p className="text-xs text-text-secondary">Enter your new secure password credential.</p>
                        </div>

                        {isSuccess ? (
                            <div className="flex flex-col gap-4 text-center items-center py-4">
                                <div className="w-10 h-10 rounded-full border border-accent-green/20 bg-accent-green/5 flex items-center justify-center text-accent-green shrink-0">
                                    <ShieldCheck className="w-5 h-5" />
                                </div>
                                <div className="flex flex-col gap-1">
                                    <h4 className="text-sm font-bold text-white">Password Reset Successful</h4>
                                    <p className="text-xs text-text-secondary leading-relaxed px-4">
                                        Your password has been successfully updated. Redirecting to Sign In...
                                    </p>
                                </div>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                                <div className="flex flex-col gap-1">
                                    <Input
                                        label="New Password"
                                        type="password"
                                        placeholder="••••••••"
                                        icon={<Lock className="w-4 h-4" />}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        error={error && error.includes("characters") ? error : undefined}
                                        required
                                    />
                                    <PasswordStrength password={password} />
                                </div>

                                <Input
                                    label="Confirm New Password"
                                    type="password"
                                    placeholder="••••••••"
                                    icon={<Lock className="w-4 h-4" />}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    error={error && error.includes("match") ? error : undefined}
                                    required
                                />

                                <Button variant="primary" size="lg" className="w-full mt-2" isLoading={isLoading} type="submit">
                                    Reset Password <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                </Button>
                            </form>
                        )}
                    </Card>
                </ScrollReveal>
            </div>
        </main>
    );
}
