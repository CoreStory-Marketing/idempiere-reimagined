"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BarChart3,
  Bell,
  Box,
  Building2,
  ClipboardList,
  Cog,
  FileSpreadsheet,
  Inbox,
  LayoutDashboard,
  Mail,
  Package,
  PackageCheck,
  PackageMinus,
  PackageSearch,
  ShoppingCart,
  Tag,
  Truck,
  Users,
  Warehouse,
  type LucideIcon,
} from "lucide-react";
import { cn } from "@/lib/utils/cn";

interface NavItem {
  href: string;
  label: string;
  icon: LucideIcon;
  badge?: string;
}

interface NavSection {
  label: string;
  items: NavItem[];
}

const sections: NavSection[] = [
  {
    label: "Operations",
    items: [
      { href: "/", label: "Dashboard", icon: LayoutDashboard },
      { href: "/orders", label: "Orders", icon: ShoppingCart },
      { href: "/inventory", label: "Inventory", icon: Package },
      { href: "/inventory/movements", label: "Stock movements", icon: PackageMinus },
      { href: "/warehouse/receipts", label: "Receipts", icon: PackageCheck },
      { href: "/warehouse/picks", label: "Picks", icon: PackageSearch, badge: "Soon" },
      { href: "/shipping", label: "Shipments", icon: Truck, badge: "Pending" },
      { href: "/notifications", label: "Notifications", icon: Bell },
    ],
  },
  {
    label: "Master data",
    items: [
      { href: "/customers", label: "Customers", icon: Users },
      { href: "/products", label: "Products", icon: Box },
      { href: "/vendors", label: "Vendors", icon: Building2 },
      { href: "/purchasing/orders", label: "Purchase orders", icon: ClipboardList },
    ],
  },
  {
    label: "Admin",
    items: [
      { href: "/admin/price-lists", label: "Price lists", icon: Tag },
      { href: "/admin/tax-rates", label: "Tax rates", icon: FileSpreadsheet },
      { href: "/admin/warehouses", label: "Warehouses", icon: Warehouse },
      { href: "/admin/carriers", label: "Carriers", icon: Truck },
      { href: "/admin/email-templates", label: "Email templates", icon: Mail },
      { href: "/admin/settings", label: "Settings", icon: Cog },
    ],
  },
  {
    label: "Insight",
    items: [
      { href: "/reports", label: "Reports", icon: BarChart3, badge: "Soon" },
    ],
  },
];

function isActive(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function Sidebar() {
  const pathname = usePathname() ?? "/";
  return (
    <aside className="hidden lg:flex w-60 shrink-0 flex-col border-r border-slate-200 bg-white">
      <div className="flex items-center gap-2 px-4 py-4 border-b border-slate-100">
        <div className="flex h-8 w-8 items-center justify-center rounded-md bg-blue-600 text-white text-sm font-bold">
          iD
        </div>
        <div className="leading-tight">
          <div className="text-sm font-semibold text-slate-900">iDempiere</div>
          <div className="text-[11px] text-slate-500">Reimagined</div>
        </div>
      </div>
      <nav className="flex-1 overflow-y-auto px-2 py-3 space-y-5">
        {sections.map((section) => (
          <div key={section.label}>
            <div className="px-2 pb-1 text-[10px] font-semibold uppercase tracking-wider text-slate-400">
              {section.label}
            </div>
            <ul className="space-y-0.5">
              {section.items.map((item) => {
                const Icon = item.icon;
                const active = isActive(pathname, item.href);
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      className={cn(
                        "flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm transition-colors",
                        active
                          ? "bg-blue-50 text-blue-700 font-medium"
                          : "text-slate-700 hover:bg-slate-100",
                      )}
                    >
                      <Icon
                        className={cn(
                          "h-4 w-4",
                          active ? "text-blue-600" : "text-slate-500",
                        )}
                        aria-hidden
                      />
                      <span className="flex-1 truncate">{item.label}</span>
                      {item.badge ? (
                        <span className="ml-auto rounded-full bg-slate-100 px-1.5 py-0.5 text-[10px] font-medium text-slate-500">
                          {item.badge}
                        </span>
                      ) : null}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </nav>
      <div className="border-t border-slate-100 px-4 py-3 text-[11px] text-slate-500">
        <div className="flex items-center gap-1.5">
          <Inbox className="h-3 w-3" aria-hidden />
          MailHog: <span className="font-mono">:8025</span>
        </div>
      </div>
    </aside>
  );
}
