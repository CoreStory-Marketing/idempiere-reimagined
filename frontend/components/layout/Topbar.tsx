"use client";

import Link from "next/link";
import { LogOut, Server } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function Topbar() {
  async function handleLogout() {
    try {
      await fetch("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore
    }
    window.location.href = "/login";
  }

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between gap-3 border-b border-slate-200 bg-white/95 px-4 backdrop-blur lg:px-6">
      <div className="flex items-center gap-3">
        <Link
          href="/"
          className="lg:hidden flex items-center gap-2 text-sm font-semibold text-slate-900"
        >
          <span className="inline-flex h-7 w-7 items-center justify-center rounded-md bg-blue-600 text-white text-xs font-bold">
            iD
          </span>
          iDempiere
        </Link>
        <span className="hidden lg:inline text-xs text-slate-500">
          Order → Shipment → Notification
        </span>
      </div>
      <div className="flex items-center gap-3">
        <span className="hidden sm:inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" aria-hidden />
          gateway healthy
        </span>
        <span className="hidden md:inline-flex items-center gap-1 text-xs text-slate-500">
          <Server className="h-3.5 w-3.5" aria-hidden />
          <span className="font-mono">admin</span>
        </span>
        <Button size="sm" variant="ghost" onClick={handleLogout}>
          <LogOut className="h-4 w-4" aria-hidden /> Sign out
        </Button>
      </div>
    </header>
  );
}
