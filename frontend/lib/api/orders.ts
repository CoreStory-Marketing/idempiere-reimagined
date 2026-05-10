import { api } from "./client";
import { mockOrder, mockOrderList } from "./mocks";
import type {
  Order,
  OrderListItem,
  OrderStatus,
  PageResponse,
} from "@/lib/types/domain";

export interface OrderListParams {
  page?: number;
  size?: number;
  status?: OrderStatus | "ALL";
  query?: string;
  customerId?: string;
}

export function listOrders(params: OrderListParams = {}): Promise<PageResponse<OrderListItem>> {
  const { page = 0, size = 20, status, query, customerId } = params;
  return api.get<PageResponse<OrderListItem>>("/orders", {
    query: {
      page,
      size,
      status: status && status !== "ALL" ? status : undefined,
      q: query || undefined,
      customerId,
    },
    mock: () => mockOrderList(page, size),
  });
}

export function getOrder(id: string): Promise<Order> {
  return api.get<Order>(`/orders/${id}`, { mock: () => mockOrder(id) });
}

export interface ConfirmOrderResponse {
  id: string;
  status: OrderStatus;
}

export function confirmOrder(id: string): Promise<ConfirmOrderResponse> {
  return api.post<ConfirmOrderResponse>(`/orders/${id}/confirm`);
}

export function cancelOrder(id: string, reason?: string): Promise<ConfirmOrderResponse> {
  return api.post<ConfirmOrderResponse>(`/orders/${id}/cancel`, { reason });
}
