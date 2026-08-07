import React from "react";
import { Inbox } from "lucide-react";
import { cn } from "@/utils/cn";

interface EmptyStateProps {
    title: string;
    description: string;
    icon?: React.ReactNode;
    action?: React.ReactNode;
    className?: string;
}

export function EmptyState({
    title,
    description,
    icon = <Inbox className="w-8 h-8 text-text-tertiary" />,
    action,
    className,
}: EmptyStateProps) {
    return (
        <div className={cn("flex flex-col items-center justify-center text-center p-8 border border-dashed border-border-color rounded-md glass-sm min-h-[300px]", className)}>
            <div className="flex items-center justify-center w-14 h-14 rounded-full bg-white/5 border border-border-color/50 mb-4 shrink-0">
                {icon}
            </div>
            <h3 className="text-md font-semibold text-white mb-1 select-none">{title}</h3>
            <p className="text-xs text-text-secondary max-w-sm mb-6 select-none">{description}</p>
            {action}
        </div>
    );
}
