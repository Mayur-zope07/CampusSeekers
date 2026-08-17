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
import { PasswordStrength } from "@/components/ui/PasswordStrength";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { GraduationCap, Mail, Lock, ArrowRight, ShieldAlert, ShieldCheck } from "lucide-react";

const registerSchema = z.object({
    email: z.string().email("Enter a valid email address"),
    password: z
        .string()
        .min(8, "Password must be at least 8 characters")
        .regex(/[A-Z]/, "Password must contain at least one uppercase letter")
        .regex(/[a-z]/, "Password must contain at least one lowercase letter")
        .regex(/[0-9]/, "Password must contain at least one digit"),
    confirmPassword: z.string(),
    role: z.enum(["STUDENT", "ADMIN"]),
    terms: z.boolean().refine((val) => val === true, "You must accept the terms and conditions"),
}).refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
});

type RegisterForm = z.infer<typeof registerSchema>;

export default function RegisterPage() {
    const { register: registerUser } = useAuth();
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [authError, setAuthError] = useState("");
    const [isSuccess, setIsSuccess] = useState(false);

    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<RegisterForm>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            role: "STUDENT",
            terms: false,
        },
    });

    const passwordVal = watch("password");

    const onSubmit = async (data: RegisterForm) => {
        setIsLoading(true);
        setAuthError("");
        try {
            await registerUser({
                email: data.email,
                password: data.password,
                role: data.role,
            });
            setIsSuccess(true);
            setTimeout(() => {
                router.push("/app/login");
            }, 2500);
        } catch (err: unknown) {
            const error = err as { response?: { data?: { message?: string } } };
            setAuthError(error.response?.data?.message || "Registration failed");
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

                <div className="w-full max-w-lg relative z-10">
                    <ScrollReveal>
                        <Card className="flex flex-col gap-6 w-full" glowColor="rgba(138, 43, 226, 0.12)">
                            <div className="flex flex-col gap-1.5 text-center items-center">
                                <GraduationCap className="w-8 h-8 text-white animate-pulse" />
                                <Badge variant="purple" glow className="mt-2">Workspace Enrollment</Badge>
                                <h2 className="text-xl font-bold text-white tracking-wide mt-1">Create Account</h2>
                            </div>

                            {authError && (
                                <div className="flex items-center gap-2.5 p-3 rounded-xs border border-accent-orange/20 bg-accent-orange/5 text-xs text-accent-orange font-medium animate-pulse">
                                    <ShieldAlert className="w-4 h-4 shrink-0" />
                                    <span>{authError}</span>
                                </div>
                            )}

                            {isSuccess && (
                                <div className="flex items-center gap-2.5 p-3 rounded-xs border border-accent-green/20 bg-accent-green/5 text-xs text-accent-green font-medium">
                                    <ShieldCheck className="w-4 h-4 shrink-0" />
                                    <span>Account created successfully! Redirecting to login...</span>
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

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="flex flex-col gap-1">
                                        <Input
                                            label="Password"
                                            type="password"
                                            placeholder="••••••••"
                                            icon={<Lock className="w-4 h-4" />}
                                            error={errors.password?.message}
                                            {...register("password")}
                                        />
                                        <PasswordStrength password={passwordVal} />
                                    </div>
                                    <Input
                                        label="Confirm Password"
                                        type="password"
                                        placeholder="••••••••"
                                        icon={<Lock className="w-4 h-4" />}
                                        error={errors.confirmPassword?.message}
                                        {...register("confirmPassword")}
                                    />
                                </div>

                                <div className="flex flex-col gap-1.5">
                                    <label className="text-xs font-semibold text-text-secondary select-none">Account Role</label>
                                    <div className="grid grid-cols-2 gap-4">
                                        <label className="flex items-center justify-between glass-sm rounded-sm p-3 border border-border-color hover:border-white/15 cursor-pointer text-xs text-white">
                                            <span>Student Candidate</span>
                                            <input type="radio" value="STUDENT" {...register("role")} className="text-accent-cyan" />
                                        </label>
                                        <label className="flex items-center justify-between glass-sm rounded-sm p-3 border border-border-color hover:border-white/15 cursor-pointer text-xs text-white">
                                            <span>Administrator</span>
                                            <input type="radio" value="ADMIN" {...register("role")} className="text-accent-purple" />
                                        </label>
                                    </div>
                                </div>

                                <label className="flex items-start gap-2.5 cursor-pointer select-none text-[11px] text-text-secondary mt-1 leading-relaxed">
                                    <input type="checkbox" {...register("terms")} className="mt-0.5 rounded-xs border-border-color bg-white/5" />
                                    <span>I accept the general terms, category weight multipliers, and privacy parameters.</span>
                                </label>
                                {errors.terms && (
                                    <p className="text-xs font-medium text-accent-orange -mt-2">{errors.terms.message}</p>
                                )}

                                <Button variant="primary" size="lg" className="w-full mt-2" isLoading={isLoading} type="submit">
                                    Create Account <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                </Button>
                            </form>

                            <div className="flex items-center justify-center gap-2 text-xs text-text-secondary select-none border-t border-border-color/30 pt-4">
                                <span>Already registered?</span>
                                <span
                                    onClick={() => router.push("/app/login")}
                                    className="text-white hover:underline transition-all cursor-pointer font-semibold"
                                >
                                    Sign In
                                </span>
                            </div>
                        </Card>
                    </ScrollReveal>
                </div>
            </main>
        </GuestRoute>
    );
}
