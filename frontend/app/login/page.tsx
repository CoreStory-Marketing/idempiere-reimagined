"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/Button";
import { Card, CardContent, CardDescription, CardTitle } from "@/components/ui/Card";

export default function LoginPage() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin() {
    setSubmitting(true);
    setError(null);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: "admin", password: "admin" }),
      });
      if (!res.ok) {
        const body = (await res.json().catch(() => null)) as { message?: string } | null;
        setError(body?.message ?? `Login failed (${res.status})`);
        setSubmitting(false);
        return;
      }
      router.replace("/");
      router.refresh();
    } catch (err) {
      setError((err as Error).message);
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-emerald-50 px-4">
      <Card className="w-full max-w-md">
        <CardContent className="px-8 py-10">
          <div className="flex flex-col items-center text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-600 text-white text-lg font-bold">
              iD
            </div>
            <CardTitle className="mt-4 text-xl">iDempiere Reimagined</CardTitle>
            <CardDescription>
              Modernized order → shipment → notification logistics
            </CardDescription>
          </div>

          <div className="mt-8 space-y-3">
            <Button
              onClick={handleLogin}
              disabled={submitting}
              className="w-full"
              size="lg"
            >
              {submitting ? "Signing in…" : "Log in as admin"}
            </Button>
            {error ? (
              <p className="text-center text-xs text-red-600">{error}</p>
            ) : (
              <p className="text-center text-xs text-slate-500">
                Demo build — single-tenant admin auth.
              </p>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
