"use client";

import React from "react";
import { Navbar } from "@/components/landing/Navbar";
import { Hero } from "@/components/landing/Hero";
import { SpotlightSearch } from "@/components/landing/SpotlightSearch";
import { Statistics } from "@/components/landing/Statistics";
import { FeatureGrid } from "@/components/landing/FeatureGrid";
import { Timeline } from "@/components/landing/Timeline";
import { RecommendationPreview } from "@/components/landing/RecommendationPreview";
import { PlatformPreview } from "@/components/landing/PlatformPreview";
import { Testimonials } from "@/components/landing/Testimonials";
import { FAQ } from "@/components/landing/FAQ";
import { CTA } from "@/components/landing/CTA";
import { Footer } from "@/components/landing/Footer";

export default function PremiumLandingPage() {
    // Generate structured schemas for SEO crawler indexing
    const jsonLd = {
        "@context": "https://schema.org",
        "@type": "WebApplication",
        "name": "CampusSeekers",
        "url": "https://campusseekers.com/",
        "description": "AI-powered college recommendations, historical cutoffs, intelligent search, and admission planning in one premium platform.",
        "applicationCategory": "EducationalApplication",
        "operatingSystem": "All",
        "offers": {
            "@type": "Offer",
            "price": "0.00",
            "priceCurrency": "INR"
        }
    };

    return (
        <main id="home" className="min-h-screen bg-primary-bg text-white overflow-hidden relative">
            {/* Structured Schema script */}
            <script
                type="application/ld+json"
                dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
            />

            {/* Layout Headers & Elements */}
            <Navbar />
            
            {/* Landing Body sections */}
            <Hero />
            <SpotlightSearch />
            <Statistics />
            <FeatureGrid />
            <Timeline />
            <RecommendationPreview />
            <PlatformPreview />
            <Testimonials />
            <FAQ />
            <CTA />
            
            {/* Footer */}
            <Footer />
        </main>
    );
}
