/**
 * Browser-facing API client.
 *
 * The browser never talks to the backend directly. All requests go through
 * the Next.js proxy route handler at `/api/proxy/[...path]`, which attaches
 * the JWT cookie server-side. CORS-free.
 *
 * In dev, when `MOCK_ENABLED=true` and the proxy returns a network/5xx
 * error, callers can opt-in via `withMock()` to return a mock fallback.
 */

export class ApiError extends Error {
  public readonly status: number;
  public readonly body?: unknown;

  constructor(status: number, message: string, body?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

export interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  query?: Record<string, string | number | boolean | undefined | null>;
  body?: unknown;
  signal?: AbortSignal;
  headers?: Record<string, string>;
  /**
   * If provided and the underlying fetch fails (network error or 5xx),
   * the mock value is returned. Only used when `process.env.MOCK_ENABLED`
   * is `"true"` AND `NODE_ENV !== "production"`.
   */
  mock?: () => unknown;
}

const PROXY_BASE = "/api/proxy";

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  const url = `${PROXY_BASE}${cleanPath}`;
  if (!query) return url;
  const params = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v === undefined || v === null) continue;
    params.set(k, String(v));
  }
  const qs = params.toString();
  return qs ? `${url}?${qs}` : url;
}

function mockEnabled(): boolean {
  if (typeof process === "undefined") return false;
  if (process.env.NODE_ENV === "production") return false;
  return process.env.MOCK_ENABLED === "true" || process.env.NEXT_PUBLIC_MOCK_ENABLED === "true";
}

export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = "GET", query, body, signal, headers = {}, mock } = options;
  const url = buildUrl(path, query);
  const init: RequestInit = {
    method,
    headers: {
      Accept: "application/json",
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...headers,
    },
    credentials: "same-origin",
    signal,
  };
  if (body !== undefined) {
    init.body = typeof body === "string" ? body : JSON.stringify(body);
  }
  let res: Response;
  try {
    res = await fetch(url, init);
  } catch (err) {
    if (mock && mockEnabled()) {
      return mock() as T;
    }
    throw new ApiError(0, `Network error: ${(err as Error).message}`);
  }
  if (!res.ok) {
    let parsed: unknown;
    try {
      parsed = await res.json();
    } catch {
      parsed = undefined;
    }
    if (res.status >= 500 && mock && mockEnabled()) {
      return mock() as T;
    }
    const message =
      (parsed && typeof parsed === "object" && "message" in parsed
        ? String((parsed as { message: unknown }).message)
        : undefined) ?? `HTTP ${res.status}`;
    throw new ApiError(res.status, message, parsed);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  const contentType = res.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return (await res.json()) as T;
  }
  return (await res.text()) as unknown as T;
}

export const api = {
  get: <T>(path: string, opts?: Omit<RequestOptions, "method" | "body">) =>
    apiRequest<T>(path, { ...opts, method: "GET" }),
  post: <T>(
    path: string,
    body?: unknown,
    opts?: Omit<RequestOptions, "method" | "body">,
  ) => apiRequest<T>(path, { ...opts, method: "POST", body }),
  put: <T>(
    path: string,
    body?: unknown,
    opts?: Omit<RequestOptions, "method" | "body">,
  ) => apiRequest<T>(path, { ...opts, method: "PUT", body }),
  patch: <T>(
    path: string,
    body?: unknown,
    opts?: Omit<RequestOptions, "method" | "body">,
  ) => apiRequest<T>(path, { ...opts, method: "PATCH", body }),
  delete: <T>(path: string, opts?: Omit<RequestOptions, "method" | "body">) =>
    apiRequest<T>(path, { ...opts, method: "DELETE" }),
};

export function isApiError(err: unknown): err is ApiError {
  return err instanceof ApiError;
}
