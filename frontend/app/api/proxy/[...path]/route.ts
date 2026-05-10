import { cookies } from "next/headers";
import { NextResponse, type NextRequest } from "next/server";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const AUTH_COOKIE = "idempiere_jwt";

function backendBaseUrl(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
}

function buildHeaders(req: NextRequest): Headers {
  const out = new Headers();
  // Forward most headers, drop hop-by-hop and host headers.
  const drop = new Set([
    "host",
    "connection",
    "content-length",
    "transfer-encoding",
    "accept-encoding",
    "cookie", // we set our own auth via Authorization header
  ]);
  req.headers.forEach((value, key) => {
    if (!drop.has(key.toLowerCase())) {
      out.set(key, value);
    }
  });
  const jwt = cookies().get(AUTH_COOKIE)?.value;
  if (jwt) {
    out.set("Authorization", `Bearer ${jwt}`);
  }
  return out;
}

async function forward(
  req: NextRequest,
  ctx: { params: { path: string[] } },
): Promise<Response> {
  const path = ctx.params.path?.join("/") ?? "";
  const search = req.nextUrl.search;
  const target = `${backendBaseUrl()}/${path}${search}`;
  const headers = buildHeaders(req);

  const init: RequestInit = {
    method: req.method,
    headers,
    redirect: "manual",
  };

  if (!["GET", "HEAD"].includes(req.method)) {
    const buf = await req.arrayBuffer();
    if (buf.byteLength > 0) {
      init.body = buf;
    }
  }

  let upstream: Response;
  try {
    upstream = await fetch(target, init);
  } catch (err) {
    return NextResponse.json(
      {
        message: `Upstream fetch failed: ${(err as Error).message}`,
        target,
      },
      { status: 502 },
    );
  }

  const respHeaders = new Headers();
  upstream.headers.forEach((value, key) => {
    if (
      !["transfer-encoding", "content-encoding", "connection"].includes(
        key.toLowerCase(),
      )
    ) {
      respHeaders.set(key, value);
    }
  });

  return new Response(upstream.body, {
    status: upstream.status,
    headers: respHeaders,
  });
}

export async function GET(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
export async function POST(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
export async function PUT(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
export async function PATCH(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
export async function DELETE(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
export async function HEAD(req: NextRequest, ctx: { params: { path: string[] } }) {
  return forward(req, ctx);
}
