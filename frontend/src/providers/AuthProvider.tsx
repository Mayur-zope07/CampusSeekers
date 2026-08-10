"use client";

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/axios";
import { useToast } from "./ToastProvider";

interface UserContext {
    id: string;
    email: string;
    role: "STUDENT" | "ADMIN";
}

interface AuthContextType {
    user: UserContext | null;
    token: string | null;
    isLoading: boolean;
    isAuthenticated: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (request: Record<string, unknown>) => Promise<void>;
    logout: () => void;
    checkProfileExistence: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<UserContext | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();
    const toast = useToast();

    const logout = useCallback(() => {
        localStorage.removeItem("access_token");
        localStorage.removeItem("user_role");
        setToken(null);
        setUser(null);
        setIsLoading(false);
        router.push("/app/login");
        toast.info("Logged out successfully");
    }, [router, toast]);

    const fetchUser = useCallback(async (accessToken: string) => {
        try {
            const res = await api.get("/api/auth/me", {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            setUser(res.data);
            setToken(accessToken);
        } catch {
            logout();
        } finally {
            setIsLoading(false);
        }
    }, [logout]);

    useEffect(() => {
        const storedToken = localStorage.getItem("access_token");
        if (storedToken) {
            fetchUser(storedToken);
        } else {
            setIsLoading(false);
        }
    }, [fetchUser]);

    const checkProfileExistenceDirect = useCallback(async (accessToken: string): Promise<boolean> => {
        try {
            await api.get("/api/profile", {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            return true;
        } catch (err: unknown) {
            const error = err as { response?: { status?: number } };
            if (error.response && error.response.status === 404) {
                return false;
            }
            return false;
        }
    }, []);

    const login = useCallback(async (email: string, password: string) => {
        setIsLoading(true);
        try {
            const res = await api.post("/api/auth/login", { email, password });
            const accessToken = res.data.accessToken;
            localStorage.setItem("access_token", accessToken);
            localStorage.setItem("user_role", res.data.role);

            await fetchUser(accessToken);
            toast.success("Login successful!");

            const profileExists = await checkProfileExistenceDirect(accessToken);
            if (profileExists) {
                router.push("/app/dashboard");
            } else {
                router.push("/app/onboarding");
            }
        } catch (err: unknown) {
            setIsLoading(false);
            const error = err as { response?: { data?: { message?: string } } };
            const msg = error.response?.data?.message || "Invalid credentials";
            toast.error(msg);
            throw err;
        }
    }, [fetchUser, checkProfileExistenceDirect, router, toast]);

    const register = useCallback(async (request: Record<string, unknown>) => {
        setIsLoading(true);
        try {
            await api.post("/api/auth/register", request);
            toast.success("Registration successful! You can now log in.");
            router.push("/app/login");
        } catch (err: unknown) {
            const error = err as { response?: { data?: { message?: string } } };
            const msg = error.response?.data?.message || "Registration failed";
            toast.error(msg);
            throw err;
        } finally {
            setIsLoading(false);
        }
    }, [router, toast]);

    const checkProfileExistence = useCallback(async (): Promise<boolean> => {
        if (!token) return false;
        return checkProfileExistenceDirect(token);
    }, [token, checkProfileExistenceDirect]);

    return (
        <AuthContext.Provider
            value={{
                user,
                token,
                isLoading,
                isAuthenticated: !!user,
                login,
                register,
                logout,
                checkProfileExistence,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
