"use client";

import React, { useEffect, useState } from "react";
import { motion, useMotionValue, useSpring } from "framer-motion";

export function CustomCursor() {
    const [isVisible, setIsVisible] = useState(false);

    const mouseX = useMotionValue(-100);
    const mouseY = useMotionValue(-100);

    const dotX = useSpring(mouseX, { damping: 45, stiffness: 550 });
    const dotY = useSpring(mouseY, { damping: 45, stiffness: 550 });

    const glowX = useSpring(mouseX, { damping: 65, stiffness: 180 });
    const glowY = useSpring(mouseY, { damping: 65, stiffness: 180 });

    useEffect(() => {
        const isTouch = window.matchMedia("(pointer: coarse)").matches;
        if (isTouch) return;

        setIsVisible(true);

        const moveCursor = (e: MouseEvent) => {
            mouseX.set(e.clientX);
            mouseY.set(e.clientY);
        };

        window.addEventListener("mousemove", moveCursor);

        return () => {
            window.removeEventListener("mousemove", moveCursor);
        };
    }, [mouseX, mouseY]);

    if (!isVisible) return null;

    return (
        <>
            <motion.div
                className="fixed top-0 left-0 w-2 h-2 bg-white rounded-full pointer-events-none z-[9999] mix-blend-difference"
                style={{
                    x: dotX,
                    y: dotY,
                    translateX: "-50%",
                    translateY: "-50%",
                }}
            />
            <motion.div
                className="fixed top-0 left-0 w-[400px] h-[400px] pointer-events-none z-[9998] rounded-full"
                style={{
                    x: glowX,
                    y: glowY,
                    translateX: "-50%",
                    translateY: "-50%",
                    background: "radial-gradient(circle, rgba(0, 240, 255, 0.05) 0%, rgba(138, 43, 226, 0.02) 50%, rgba(0,0,0,0) 70%)",
                }}
            />
        </>
    );
}
