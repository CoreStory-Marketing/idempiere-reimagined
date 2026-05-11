import { cookies } from "next/headers";
import { NextResponse, type NextRequest } from "next/server";

export const runtime = "nodejs";

const AUTH_COOKIE = "idempiere_jwt";
const ONE_DAY_SECONDS = 60 * 60 * 24;

function backendBaseUrl(): string {
  // Server-side env var first (e.g., `http://api-gateway:8080` inside Docker).
  // Falls back to the browser-side public URL (only useful when running Next.js
  // outside Docker, where the gateway is reachable as localhost:8080).
  return (
    process.env.BACKEND_INTERNAL_URL ||
    process.env.NEXT_PUBLIC_API_BASE_URL ||
    "http://localhost:8080"
  );
}

interface LoginRequestBody {
  username?: string;
  password?: string;
}

interface BackendLoginResponse {
  token?: string;
  accessToken?: string;
  jwt?: string;
}

export async function POST(req: NextRequest): Promise<Response> {
  let payload: LoginRequestBody;
  try {
    payload = (await req.json()) as LoginRequestBody;
  } catch {
    payload = {};
  }
  const username = payload.username || "admin";
  const password = payload.password || "admin";

  let upstream: Response;
  try {
    upstream = await fetch(`${backendBaseUrl()}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ username, password }),
    });
  } catch (err) {
    // In dev with MOCK_ENABLED, fall back to a fake token so the UI flows.
    const mockEnabled =
      process.env.NODE_ENV !== "production" &&
      (process.env.MOCK_ENABLED === "true" ||
        process.env.NEXT_PUBLIC_MOCK_ENABLED === "true");
    if (mockEnabled) {
      const res = NextResponse.json({ ok: true, mock: true });
      res.cookies.set(AUTH_COOKIE, "dev-mock-token", {
        httpOnly: true,
        sameSite: "lax",
        secure: process.env.NODE_ENV === "production",
        path: "/",
        maxAge: ONE_DAY_SECONDS,
      });
      return res;
    }
    return NextResponse.json(
      { message: `Upstream auth failed: ${(err as Error).message}` },
      { status: 502 },
    );
  }

  if (!upstream.ok) {
    let body: unknown;
    try {
      body = await upstream.json();
    } catch {
      body = undefined;
    }
    return NextResponse.json(
      {
        message:
          (body && typeof body === "object" && "message" in body
            ? String((body as { message: unknown }).message)
            : undefined) ?? "Login failed",
      },
      { status: upstream.status },
    );
  }

  const data = (await upstream.json()) as BackendLoginResponse;
  const token = data.token ?? data.accessToken ?? data.jwt;
  if (!token) {
    return NextResponse.json(
      { message: "Login response did not include a token" },
      { status: 502 },
    );
  }

  const res = NextResponse.json({ ok: true });
  res.cookies.set(AUTH_COOKIE, token, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: ONE_DAY_SECONDS,
  });
  return res;
}

export function GET(): Response {
  const jwt = cookies().get(AUTH_COOKIE)?.value;
  return NextResponse.json({ authenticated: Boolean(jwt) });
}
