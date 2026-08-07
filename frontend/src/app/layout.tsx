import type { Metadata } from "next";
import { Inter, Orbitron } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "@/providers/ThemeProvider";
import { QueryProvider } from "@/providers/QueryProvider";
import { ToastProvider } from "@/providers/ToastProvider";
import { LenisProvider } from "@/providers/LenisProvider";
import { CustomCursor } from "@/components/animations/CustomCursor";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

const orbitron = Orbitron({
  variable: "--font-futuristic",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "CampusSeekers - Premium College Admission Platform",
  description: "Enterprise Smart College Recommendation & Workflow Platform.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body className={`${inter.variable} ${orbitron.variable} antialiased`}>
        <QueryProvider>
          <ThemeProvider>
            <ToastProvider>
              <LenisProvider>
                <CustomCursor />
                <div className="noise-overlay" />
                {children}
              </LenisProvider>
            </ToastProvider>
          </ThemeProvider>
        </QueryProvider>
      </body>
    </html>
  );
}
