import { NextResponse, type NextRequest } from "next/server";

const AUTH_COOKIE = "idempiere_jwt";

/**
 * Redirects unauthenticated browser requests to /login.
 *
 * API routes (proxy, auth) are not redirected — the proxy attaches the
 * JWT cookie if present, and the auth routes manage the cookie themselves.
 */
export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;
  // Let through:
  //   - /login (the splash)
  //   - /api/* (auth + proxy + any future API routes)
  //   - static assets
  if (
    pathname === "/login" ||
    pathname.startsWith("/api/") ||
    pathname.startsWith("/_next/") ||
    pathname === "/favicon.ico" ||
    pathname === "/logo.svg"
  ) {
    return NextResponse.next();
  }
  const token = req.cookies.get(AUTH_COOKIE)?.value;
  if (!token) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    if (pathname !== "/") {
      url.searchParams.set("from", pathname);
    }
    return NextResponse.redirect(url);
  }
  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!_next/static|_next/image|favicon.ico|logo.svg).*)",
  ],
};
