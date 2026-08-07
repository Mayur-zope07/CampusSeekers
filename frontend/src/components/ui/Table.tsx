import React from "react";
import { cn } from "@/utils/cn";

export function TableContainer({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <div className={cn("w-full overflow-x-auto rounded-md border border-border-color glass-sm shadow-md", className)}>
            <table className="w-full border-collapse text-left text-sm text-text-secondary">
                {children}
            </table>
        </div>
    );
}

export function TableHead({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <thead className={cn("bg-white/2 border-b border-border-color text-xs font-semibold text-white uppercase tracking-wider", className)}>
            {children}
        </thead>
    );
}

export function TableBody({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <tbody className={cn("divide-y divide-border-color/30", className)}>
            {children}
        </tbody>
    );
}

export function TableRow({ children, className, ...props }: React.HTMLAttributes<HTMLTableRowElement>) {
    return (
        <tr className={cn("hover:bg-white/2 transition-colors duration-200", className)} {...props}>
            {children}
        </tr>
    );
}

export function TableHeaderCell({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <th className={cn("px-6 py-4 font-semibold select-none", className)}>
            {children}
        </th>
    );
}

export function TableCell({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <td className={cn("px-6 py-4 text-xs font-medium text-text-secondary select-none", className)}>
            {children}
        </td>
    );
}
