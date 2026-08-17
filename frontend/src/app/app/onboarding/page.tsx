"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Dropdown } from "@/components/ui/Dropdown";
import { ProgressBar } from "@/components/ui/ProgressBar";
import { ScrollReveal } from "@/components/animations/ScrollReveal";
import { api } from "@/lib/axios";
import { useToast } from "@/providers/ToastProvider";
import {
    ArrowLeft,
    ArrowRight,
    ShieldCheck,
    AlertCircle
} from "lucide-react";

export default function OnboardingPage() {
    const router = useRouter();
    const toast = useToast();
    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    // Onboarding Form States
    const [formData, setFormData] = useState({
        // Step 1: Basic
        firstName: "",
        lastName: "",
        phone: "",
        gender: "MALE",
        // Step 2: Category
        category: "OPEN",
        subCategory: "",
        homeState: "Maharashtra",
        homeDistrict: "",
        // Step 3: Exam
        examName: "MHT_CET",
        examYear: 2025,
        // Step 4: Scores
        rank: "",
        percentile: "",
        marks: "",
        // Step 5: Branches
        branches: [] as string[],
        // Step 6: Cities
        cities: [] as string[],
        // Step 7: Budget
        maxFees: "150000",
        // Step 8: College Type
        collegeType: "GOVERNMENT",
    });

    // Auto-load progress from LocalStorage
    useEffect(() => {
        const cached = localStorage.getItem("onboarding_progress");
        if (cached) {
            try {
                setFormData(JSON.parse(cached));
            } catch {
                // Ignore parsing errors
            }
        }
    }, []);

    // Auto-save progress
    const updateForm = (fields: Partial<typeof formData>) => {
        setFormData((prev) => {
            const next = { ...prev, ...fields };
            localStorage.setItem("onboarding_progress", JSON.stringify(next));
            return next;
        });
    };

    const validateStep = (): boolean => {
        setErrorMsg("");
        if (step === 1) {
            if (!formData.firstName) { setErrorMsg("First name is required"); return false; }
            if (!formData.lastName) { setErrorMsg("Last name is required"); return false; }
            if (!formData.phone || !/^[0-9]{10}$/.test(formData.phone)) {
                setErrorMsg("Phone number must be a valid 10-digit number");
                return false;
            }
        }
        if (step === 2) {
            if (!formData.homeDistrict) { setErrorMsg("Home district is required"); return false; }
        }
        if (step === 4) {
            if (!formData.rank || isNaN(Number(formData.rank))) { setErrorMsg("Valid rank is required"); return false; }
            if (!formData.percentile || isNaN(Number(formData.percentile)) || Number(formData.percentile) < 0 || Number(formData.percentile) > 100) {
                setErrorMsg("Percentile must be between 0.00 and 100.00");
                return false;
            }
        }
        return true;
    };

    const handleNext = () => {
        if (validateStep()) {
            setStep((prev) => Math.min(prev + 1, 10));
        }
    };

    const handleBack = () => {
        setStep((prev) => Math.max(prev - 1, 1));
    };

    const handleSubmit = async () => {
        setIsLoading(true);
        setErrorMsg("");
        try {
            // 1. Post Profile Details
            await api.post("/api/profile", {
                firstName: formData.firstName,
                lastName: formData.lastName,
                phone: formData.phone,
                gender: formData.gender,
                category: formData.category,
                subCategory: formData.subCategory || null,
                homeState: formData.homeState,
                homeDistrict: formData.homeDistrict,
            });

            // 2. Post Exam Scores
            await api.post("/api/profile/scores", {
                examName: formData.examName,
                examYear: Number(formData.examYear),
                rank: Number(formData.rank),
                percentile: Number(formData.percentile),
                marks: formData.marks ? Number(formData.marks) : null,
            });

            toast.success("Profile onboarding complete!");
            localStorage.removeItem("onboarding_progress");
            setStep(10); // Go to finish step
        } catch (err: unknown) {
            const error = err as { response?: { data?: { message?: string } } };
            const msg = error.response?.data?.message || "Failed to submit onboarding profile";
            setErrorMsg(msg);
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    };

    const toggleBranch = (branch: string) => {
        const next = formData.branches.includes(branch)
            ? formData.branches.filter((b) => b !== branch)
            : [...formData.branches, branch];
        updateForm({ branches: next });
    };

    const toggleCity = (city: string) => {
        const next = formData.cities.includes(city)
            ? formData.cities.filter((c) => c !== city)
            : [...formData.cities, city];
        updateForm({ cities: next });
    };

    const progressValue = (step / 10) * 100;

    return (
        <ProtectedRoute>
            <main className="min-h-screen bg-primary-bg flex items-center justify-center p-6 relative overflow-y-auto py-12">
                <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0">
                    <div className="absolute top-[10%] left-[20%] w-96 h-96 bg-accent-cyan rounded-full blur-[150px] animate-ambient-breathe" />
                    <div className="absolute bottom-[10%] right-[20%] w-96 h-96 bg-accent-purple rounded-full blur-[150px] animate-ambient-breathe" />
                </div>

                <div className="w-full max-w-xl relative z-10">
                    <ScrollReveal>
                        <Card className="flex flex-col gap-6 w-full" glowColor="rgba(0, 240, 255, 0.12)" allowOverflow={true}>
                            {/* Header & Progress */}
                            {step < 10 && (
                                <div className="flex flex-col gap-3">
                                    <div className="flex justify-between items-center text-xs font-bold text-text-secondary select-none">
                                        <span>Workspace Setup</span>
                                        <span>Step {step} of 9</span>
                                    </div>
                                    <ProgressBar value={progressValue} />
                                </div>
                            )}

                            {errorMsg && (
                                <div className="flex items-center gap-2.5 p-3 rounded-xs border border-accent-orange/20 bg-accent-orange/5 text-xs text-accent-orange font-medium animate-pulse">
                                    <AlertCircle className="w-4 h-4 shrink-0" />
                                    <span>{errorMsg}</span>
                                </div>
                            )}

                            {/* Step Canvas */}
                            <div className="min-h-[220px] flex flex-col justify-center">
                                {step === 1 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Basic Details</h3>
                                            <p className="text-xs text-text-secondary">Enter your personal registration details.</p>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <Input label="First Name" value={formData.firstName} onChange={(e) => updateForm({ firstName: e.target.value })} required />
                                            <Input label="Last Name" value={formData.lastName} onChange={(e) => updateForm({ lastName: e.target.value })} required />
                                        </div>
                                        <Input label="Phone Number" placeholder="10-digit number" value={formData.phone} onChange={(e) => updateForm({ phone: e.target.value })} required />
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Gender</label>
                                            <Dropdown
                                                options={[
                                                    { label: "Male", value: "MALE" },
                                                    { label: "Female", value: "FEMALE" },
                                                    { label: "Other", value: "OTHER" }
                                                ]}
                                                selected={formData.gender}
                                                onChange={(val) => updateForm({ gender: val })}
                                            />
                                        </div>
                                    </div>
                                )}

                                {step === 2 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Category & Region</h3>
                                            <p className="text-xs text-text-secondary">Select details for cutoff allocation metrics.</p>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div className="flex flex-col gap-1.5">
                                                <label className="text-xs font-semibold text-text-secondary">Category</label>
                                                <Dropdown
                                                    options={[
                                                        { label: "OPEN", value: "OPEN" },
                                                        { label: "OBC", value: "OBC" },
                                                        { label: "SC", value: "SC" },
                                                        { label: "ST", value: "ST" },
                                                        { label: "EWS", value: "EWS" }
                                                    ]}
                                                    selected={formData.category}
                                                    onChange={(val) => updateForm({ category: val })}
                                                />
                                            </div>
                                            <Input label="Sub Category" placeholder="e.g. Caste" value={formData.subCategory} onChange={(e) => updateForm({ subCategory: e.target.value })} />
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <Input label="Home State" value={formData.homeState} onChange={(e) => updateForm({ homeState: e.target.value })} required />
                                            <Input label="Home District" placeholder="e.g. Pune" value={formData.homeDistrict} onChange={(e) => updateForm({ homeDistrict: e.target.value })} required />
                                        </div>
                                    </div>
                                )}

                                {step === 3 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Target Exam</h3>
                                            <p className="text-xs text-text-secondary">Choose the entrance test scores to evaluate.</p>
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Exam Name</label>
                                            <Dropdown
                                                options={[
                                                    { label: "MHT-CET (State Exam)", value: "MHT_CET" },
                                                    { label: "JEE Main (National)", value: "JEE_MAIN" }
                                                ]}
                                                selected={formData.examName}
                                                onChange={(val) => updateForm({ examName: val })}
                                            />
                                        </div>
                                        <Input label="Exam Year" type="number" value={formData.examYear} onChange={(e) => updateForm({ examYear: Number(e.target.value) })} required />
                                    </div>
                                )}

                                {step === 4 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Exam Score</h3>
                                            <p className="text-xs text-text-secondary">Provide percentile or ranks to evaluate matches.</p>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <Input label="State Merit Rank" type="number" placeholder="e.g. 4510" value={formData.rank} onChange={(e) => updateForm({ rank: e.target.value })} required />
                                            <Input label="Percentile Score" type="number" placeholder="e.g. 98.42" value={formData.percentile} onChange={(e) => updateForm({ percentile: e.target.value })} required />
                                        </div>
                                        <Input label="Marks Obtained (Optional)" type="number" value={formData.marks} onChange={(e) => updateForm({ marks: e.target.value })} />
                                    </div>
                                )}

                                {step === 5 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Preferred Branches</h3>
                                            <p className="text-xs text-text-secondary">Select academic fields you want to shortlist.</p>
                                        </div>
                                        <div className="flex flex-wrap gap-2.5">
                                            {["Computer Science", "Information Technology", "Electronics & Telecom", "Electrical Engineering", "Mechanical", "Civil"].map((branch) => {
                                                const isSelected = formData.branches.includes(branch);
                                                return (
                                                    <button
                                                        key={branch}
                                                        onClick={() => toggleBranch(branch)}
                                                        className={`glass-sm rounded-sm px-4 py-2.5 text-xs font-semibold transition-all border cursor-pointer select-none ${isSelected ? "border-accent-cyan bg-accent-cyan/10 text-white" : "border-border-color text-text-secondary hover:text-white"}`}
                                                    >
                                                        {branch}
                                                    </button>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}

                                {step === 6 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Preferred Locations</h3>
                                            <p className="text-xs text-text-secondary">Select cities you prefer to study in.</p>
                                        </div>
                                        <div className="flex flex-wrap gap-2.5">
                                            {["Pune", "Mumbai", "Nagpur", "Nashik", "Aurangabad", "Sangli"].map((city) => {
                                                const isSelected = formData.cities.includes(city);
                                                return (
                                                    <button
                                                        key={city}
                                                        onClick={() => toggleCity(city)}
                                                        className={`glass-sm rounded-sm px-4 py-2.5 text-xs font-semibold transition-all border cursor-pointer select-none ${isSelected ? "border-accent-purple bg-accent-purple/10 text-white" : "border-border-color text-text-secondary hover:text-white"}`}
                                                    >
                                                        {city}
                                                    </button>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}

                                {step === 7 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Annual Fees Budget</h3>
                                            <p className="text-xs text-text-secondary">What is the maximum yearly budget range you can allocate?</p>
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">Max Fees Per Year</label>
                                            <Dropdown
                                                options={[
                                                    { label: "Under 50,000 INR", value: "50000" },
                                                    { label: "Under 1,00,000 INR", value: "100000" },
                                                    { label: "Under 1,50,000 INR", value: "150000" },
                                                    { label: "No Limit", value: "1000000" }
                                                ]}
                                                selected={formData.maxFees}
                                                onChange={(val) => updateForm({ maxFees: val })}
                                            />
                                        </div>
                                    </div>
                                )}

                                {step === 8 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">College Affiliation</h3>
                                            <p className="text-xs text-text-secondary">Select default college type category filters.</p>
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <label className="text-xs font-semibold text-text-secondary">College Type</label>
                                            <Dropdown
                                                options={[
                                                    { label: "Government / Government Aided", value: "GOVERNMENT" },
                                                    { label: "Private Un-Aided", value: "PRIVATE" },
                                                    { label: "All Types", value: "ALL" }
                                                ]}
                                                selected={formData.collegeType}
                                                onChange={(val) => updateForm({ collegeType: val })}
                                            />
                                        </div>
                                    </div>
                                )}

                                {step === 9 && (
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-0.5">
                                            <h3 className="text-md font-bold text-white">Review Profile</h3>
                                            <p className="text-xs text-text-secondary">Validate details before synchronizing database records.</p>
                                        </div>
                                        <div className="flex flex-col gap-2.5 max-h-60 overflow-y-auto border border-border-color/30 rounded-md p-4 bg-white/2 text-xs">
                                            <div className="flex justify-between border-b border-border-color/20 pb-2">
                                                <span className="text-text-tertiary">Student Name</span>
                                                <span className="text-white font-semibold">{formData.firstName} {formData.lastName}</span>
                                            </div>
                                            <div className="flex justify-between border-b border-border-color/20 pb-2">
                                                <span className="text-text-tertiary">Category & State</span>
                                                <span className="text-white font-semibold">{formData.category} ({formData.homeState})</span>
                                            </div>
                                            <div className="flex justify-between border-b border-border-color/20 pb-2">
                                                <span className="text-text-tertiary">Exam & Score</span>
                                                <span className="text-white font-semibold">{formData.examName} ({formData.percentile}%)</span>
                                            </div>
                                            <div className="flex justify-between border-b border-border-color/20 pb-2">
                                                <span className="text-text-tertiary">Branches</span>
                                                <span className="text-white font-semibold max-w-[200px] text-right truncate">
                                                    {formData.branches.join(", ") || "Any"}
                                                </span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-text-tertiary">Locations</span>
                                                <span className="text-white font-semibold max-w-[200px] text-right truncate">
                                                    {formData.cities.join(", ") || "Any"}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {step === 10 && (
                                    <div className="flex flex-col gap-4 text-center items-center py-6 select-none">
                                        <div className="w-12 h-12 rounded-full border border-accent-green/20 bg-accent-green/5 flex items-center justify-center text-accent-green shrink-0 animate-bounce">
                                            <ShieldCheck className="w-6 h-6" />
                                        </div>
                                        <div className="flex flex-col gap-1.5">
                                            <h3 className="text-md font-bold text-white">Setup Completed</h3>
                                            <p className="text-xs text-text-secondary leading-relaxed max-w-sm">
                                                Your profile settings, exam percentiles, and category matrices have been configured.
                                            </p>
                                        </div>
                                        <Button variant="primary" size="lg" className="mt-4" onClick={() => router.push("/app/dashboard")}>
                                            Explore Dashboard <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                        </Button>
                                    </div>
                                )}
                            </div>

                            {/* Navigation Actions */}
                            {step < 10 && (
                                <div className="flex justify-between items-center border-t border-border-color/30 pt-4 mt-2">
                                    <Button variant="secondary" size="md" onClick={handleBack} disabled={step === 1 || isLoading}>
                                        <ArrowLeft className="w-4 h-4 mr-1.5 shrink-0" /> Back
                                    </Button>
                                    {step === 9 ? (
                                        <Button variant="primary" size="md" onClick={handleSubmit} isLoading={isLoading}>
                                            Complete Setup <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                        </Button>
                                    ) : (
                                        <Button variant="primary" size="md" onClick={handleNext}>
                                            Next <ArrowRight className="w-4 h-4 ml-1.5 shrink-0" />
                                        </Button>
                                    )}
                                </div>
                            )}
                        </Card>
                    </ScrollReveal>
                </div>
            </main>
        </ProtectedRoute>
    );
}
