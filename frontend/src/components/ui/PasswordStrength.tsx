"use client";

import React from "react";
import { cn } from "@/utils/cn";

interface PasswordStrengthProps {
    password?: string;
}

export function PasswordStrength({ password = "" }: PasswordStrengthProps) {
    const getStrength = () => {
        let score = 0;
        if (!password) return 0;
        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;
        return score;
    };

    const strength = getStrength();

    const colors = {
        0: "bg-neutral-800",
        1: "bg-accent-orange",
        2: "bg-accent-orange/80",
        3: "bg-accent-cyan/80",
        4: "bg-accent-green",
    }[strength];

    const labels = {
        0: "Too Short",
        1: "Weak",
        2: "Fair",
        3: "Good",
        4: "Strong",
    }[strength];

    return (
        <div className="flex flex-col gap-1.5 w-full mt-1.5 select-none">
            <div className="flex justify-between items-center text-[10px] font-bold text-text-secondary uppercase">
                <span>Password Strength</span>
                <span className={cn(
                    strength === 4 && "text-accent-green",
                    strength === 3 && "text-accent-cyan",
                    strength === 2 && "text-accent-orange/80",
                    strength === 1 && "text-accent-orange"
                )}>{labels}</span>
            </div>
            <div className="grid grid-cols-4 gap-1.5 h-1 w-full bg-white/5 rounded-full overflow-hidden">
                {[1, 2, 3, 4].map((index) => (
                    <div
                        key={index}
                        className={cn(
                            "h-full rounded-full transition-all duration-300",
                            index <= strength ? colors : "bg-neutral-800"
                        )}
                    />
                ))}
            </div>
        </div>
    );
}
