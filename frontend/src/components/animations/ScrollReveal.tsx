"use client";

import React from "react";
import { motion } from "framer-motion";

interface ScrollRevealProps {
    children: React.ReactNode;
    delay?: number;
    duration?: number;
    yOffset?: number;
}

export function ScrollReveal({ children, delay = 0, duration = 0.6, yOffset = 20 }: ScrollRevealProps) {
    return (
        <motion.div
            initial={{ opacity: 0, y: yOffset, filter: "blur(4px)" }}
            whileInView={{ opacity: 1, y: 0, filter: "blur(0px)" }}
            viewport={{ once: true, margin: "-10%" }}
            transition={{
                duration: duration,
                delay: delay,
                ease: [0.16, 1, 0.3, 1],
            }}
        >
            {children}
        </motion.div>
    );
}
