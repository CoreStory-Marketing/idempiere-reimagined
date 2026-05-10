import { api, ApiError } from "./client";
import {
  mockCarriers,
  mockEmailTemplates,
  mockPriceLists,
  mockSettings,
  mockTaxCategories,
} from "./mocks";
import type {
  Carrier,
  EmailTemplate,
  PriceList,
  SystemSettings,
  TaxCategory,
} from "@/lib/types/domain";

export function listPriceLists(): Promise<PriceList[]> {
  return api.get<PriceList[]>("/price-lists", { mock: () => mockPriceLists() });
}

export function listTaxCategories(): Promise<TaxCategory[]> {
  return api.get<TaxCategory[]>("/tax-categories", {
    mock: () => mockTaxCategories(),
  });
}

export function listCarriers(): Promise<Carrier[]> {
  return api.get<Carrier[]>("/carriers", { mock: () => mockCarriers() });
}

export function listEmailTemplates(): Promise<EmailTemplate[]> {
  return api.get<EmailTemplate[]>("/email-templates", {
    mock: () => mockEmailTemplates(),
  });
}

export function getEmailTemplate(id: string): Promise<EmailTemplate> {
  return api.get<EmailTemplate>(`/email-templates/${id}`, {
    mock: () => {
      const all = mockEmailTemplates();
      return all.find((t) => t.id === id) ?? all[0]!;
    },
  });
}

export interface RenderedTemplate {
  subject: string;
  bodyHtml: string;
  bodyText?: string;
}

/**
 * Render preview — currently 501 until SHIP-101 lands.
 * Throws ApiError(501) so the page can show a "preview pending" message.
 */
export function renderEmailTemplate(
  id: string,
  variables: Record<string, string>,
): Promise<RenderedTemplate> {
  return api.post<RenderedTemplate>(`/email-templates/${id}/render`, {
    variables,
  });
}

export function isPreviewPending(err: unknown): boolean {
  return err instanceof ApiError && err.status === 501;
}

export function getSystemSettings(): Promise<SystemSettings> {
  return api.get<SystemSettings>("/settings", { mock: () => mockSettings() });
}
