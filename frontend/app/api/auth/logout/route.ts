import { NextResponse } from "next/server";

export const runtime = "nodejs";
const AUTH_COOKIE = "idempiere_jwt";

export function POST(): Response {
  const res = NextResponse.json({ ok: true });
  res.cookies.set(AUTH_COOKIE, "", {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 0,
  });
  return res;
}
