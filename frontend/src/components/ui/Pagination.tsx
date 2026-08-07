"use client";

import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "./Button";
import { cn } from "@/utils/cn";

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    className?: string;
}

export function Pagination({ currentPage, totalPages, onPageChange, className }: PaginationProps) {
    const isFirst = currentPage === 0;
    const isLast = currentPage === totalPages - 1;

    return (
        <div className={cn("flex items-center justify-between gap-4 mt-6", className)}>
            <span className="text-xs text-text-secondary select-none">
                Page {currentPage + 1} of {totalPages || 1}
            </span>
            <div className="flex gap-2">
                <Button
                    variant="secondary"
                    size="sm"
                    disabled={isFirst}
                    onClick={() => onPageChange(currentPage - 1)}
                >
                    <ChevronLeft className="w-4 h-4" />
                    <span>Prev</span>
                </Button>
                <Button
                    variant="secondary"
                    size="sm"
                    disabled={isLast}
                    onClick={() => onPageChange(currentPage + 1)}
                >
                    <span>Next</span>
                    <ChevronRight className="w-4 h-4" />
                </Button>
            </div>
        </div>
    );
}
