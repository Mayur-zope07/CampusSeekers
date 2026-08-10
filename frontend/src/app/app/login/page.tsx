"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useAuth } from "@/providers/AuthProvider";
import { GuestRoute } from "@/components/auth/GuestRoute";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { GraduationCap, Mail, Lock, ArrowRight, ShieldAlert, Eye, EyeOff, AlertTriangle } from "lucide-react";

const loginSchema = z.object({
    email: z.string().email("Enter a valid email address"),
    password: z.string().min(6, "Password must be at least 6 characters"),
});

type LoginForm = z.infer<typeof loginSchema>;

export default function LoginPage() {
    const { login } = useAuth();
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [authError, setAuthError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [capsLockActive, setCapsLockActive] = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginForm>({
        resolver: zodResolver(loginSchema),
    });

    const checkCapsLock = (e: React.KeyboardEvent) => {
        setCapsLockActive(e.getModifierState("CapsLock"));
    };

    const onSubmit = async (data: LoginForm) => {
        setIsLoading(true);
        setAuthError("");
        try {
            await login(data.email, data.password);
        } catch (err: unknown) {
            const error = err as { response?: { data?: { message?: string } } };
            setAuthError(error.response?.data?.message || "Invalid credentials");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <GuestRoute>
            <main className="min-h-screen bg-primary-bg flex items-center justify-center p-6 relative overflow-hidden">
                <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0">
                    <div className="absolute top-[10%] left-[20%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px] animate-ambient-breathe" />
                    <div className="absolute bottom-[10%] right-[20%] w-96 h-96 bg-accent-purple rounded-full blur-[150px] animate-ambient-breathe" />
                </div>

                <div className="w-full max-w-5xl grid grid-cols-1 md:grid-cols-2 gap-12 items-center relative z-10">
                    <div className="hidden md:flex flex-col gap-6 text-left select-none">
                        <div className="flex items-center gap-2">
                            <GraduationCap className="w-6 h-6 text-white animate-pulse" />
                            <span className="font-futuristic font-extralight text-md tracking-[0.25em] text-white uppercase">
                                CampusSeekers
                            </span>
                        </div>
                        <h1 className="text-4xl md:text-5xl font-extrabold text-white leading-tight">
                            Access your<br />
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-cyan via-accent-purple to-accent-orange">
                                admission workspace.
                            </span>
                        </h1>
                        <p className="text-xs text-text-secondary leading-relaxed max-w-sm">
                            Analyze historical cutoffs, simulate seat allocations, and finalize your college preferences in one premium environment.
                        </p>
                    </div>

                    <ScrollReveal delay={0.15}>
                        <Card className="flex flex-col gap-6 w-full" glowColor="rgba(0, 240, 255, 0.12)">
                            <div className="flex flex-col gap-1.5">
                                <Badge variant="cyan" glow className="w-fit">Welcome Back</Badge>
                                <h2 className="text-xl font-bold text-white tracking-wide mt-1">Authenticate Credentials</h2>
                                <p className="text-xs text-text-secondary">Enter your registered details to continue.</p>
                            </div>

                            {authError && (
                                <div className="flex items-center gap-2.5 p-3 rounded-xs border border-accent-orange/20 bg-accent-orange/5 text-xs text-accent-orange font-medium animate-pulse">
                                    <ShieldAlert className="w-4 h-4 shrink-0" />
                                    <span>{authError}</span>
                                </div>
                            )}

                            <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
                                <Input
                                    label="Email Address"
                                    placeholder="name@example.com"
                                    icon={<Mail className="w-4 h-4" />}
                                    error={errors.email?.message}
                                    {...register("email")}
                                />
                                <div className="flex flex-col gap-1 w-full relative">
                                    <div className="relative">
                                        <Input
                                            label="Password"
                                            type={showPassword ? "text" : "password"}
                                            placeholder="••••••••"
                                            icon={<Lock className="w-4 h-4" />}
                                            error={errors.password?.message}
                                            onKeyDown={checkCapsLock}
                                            onKeyUp={checkCapsLock}
                                            {...register("password")}
                                        />
                                        <button
                                            type="button"
                                            onClick={() => setShowPassword(!showPassword)}
                                            className="absolute right-3.5 top-[38px] text-text-secondary hover:text-white transition-colors cursor-pointer"
                                        >
                                            {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                        </button>
                                    </div>

                                    {capsLockActive && (
                                        <div className="flex items-center gap-1.5 text-[10px] text-accent-orange font-bold uppercase tracking-wider mt-1.5 select-none">
                                            <AlertTriangle className="w-3.5 h-3.5" />
                                            <span>Caps Lock is Active</span>
                                        </div>
                                    )}

                                    <div className="flex items-center justify-between text-[11px] font-semibold text-text-secondary mt-2">
                                        <label className="flex items-center gap-2 cursor-pointer select-none">
                                            <input type="checkbox" className="rounded-xs border-border-color bg-white/5" />
                                            <span>Remember me</span>
                                        </label>
                                        <span
                                            onClick={() => router.push("/app/forgot-password")}
                                            className="hover:text-white transition-colors cursor-pointer"
                                        >
                                            Forgot password?
                                        </span>
                                    </div>
                                </div>

                                <Button variant="primary" size="lg" className="w-full mt-2" isLoading={isLoading} type="submit">
                                    Sign In <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                </Button>
                            </form>

                            {/* Google login placeholder */}
                            <div className="flex flex-col gap-3 border-t border-border-color/20 pt-4 mt-2">
                                <div className="relative flex justify-center text-[10px] uppercase select-none font-bold text-text-tertiary">
                                    <span className="bg-primary-bg px-2">Or continue with</span>
                                </div>
                                <Button variant="secondary" className="w-full gap-2 text-xs font-semibold" type="button">
                                    <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24">
                                        <path fill="#EA4335" d="M12 5.04c1.66 0 3.2.57 4.38 1.69l3.27-3.27C17.67 1.54 14.98 1 12 1 7.35 1 3.37 3.67 1.39 7.56l3.89 3.02C6.23 7.42 8.89 5.04 12 5.04z" />
                                        <path fill="#4285F4" d="M23.49 12.27c0-.81-.07-1.59-.2-2.34H12v4.47h6.46c-.28 1.48-1.11 2.74-2.37 3.58l3.69 2.87c2.16-1.99 3.71-4.92 3.71-8.58z" />
                                        <path fill="#FBBC05" d="M5.28 14.78c-.24-.72-.38-1.49-.38-2.28s.14-1.56.38-2.28L1.39 7.2A11.96 11.96 0 000 12c0 1.77.39 3.46 1.09 4.98l4.19-3.2z" />
                                        <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.69-2.87c-1.11.75-2.53 1.19-4.27 1.19-3.11 0-5.77-2.38-6.72-5.54l-3.89 3.02C3.37 20.33 7.35 23 12 23z" />
                                    </svg>
                                    Google (UI Prototype)
                                </Button>
                            </div>

                            <div className="flex items-center justify-center gap-2 text-xs text-text-secondary select-none border-t border-border-color/30 pt-4">
                                <span>Don&apos;t have an account?</span>
                                <span
                                    onClick={() => router.push("/app/register")}
                                    className="text-white hover:underline transition-all cursor-pointer font-semibold"
                                >
                                    Register Now
                                </span>
                            </div>
                        </Card>
                    </ScrollReveal>
                </div>
            </main>
        </GuestRoute>
    );
}
