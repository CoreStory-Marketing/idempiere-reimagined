/**
 * Domain types — mirror the Spring Boot DTOs across the 5 services.
 *
 * Keep these aligned with `domain-common` and the per-service model packages.
 * When the backend evolves a field, update here too.
 */

export type OrderStatus =
  | "DRAFT"
  | "PENDING"
  | "CONFIRMED"
  | "SHIPPED"
  | "INVOICED"
  | "COMPLETE"
  | "CANCELLED"
  | "VOIDED";

export type DocumentStatus =
  | "DRAFT"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CLOSED"
  | "VOIDED"
  | "REVERSED";

export interface PageResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

export interface Money {
  amount: string;
  currency: string;
}

export interface OrderLine {
  id: string;
  lineNo: number;
  productId: string;
  productSku: string;
  productName: string;
  quantity: string;
  uom: string;
  unitPrice: Money;
  lineTotal: Money;
  taxAmount?: Money;
  description?: string;
}

export interface StatusHistoryEntry {
  status: OrderStatus;
  changedAt: string;
  changedBy: string;
  note?: string;
}

export interface CustomerSummary {
  id: string;
  code: string;
  name: string;
  email?: string;
  phone?: string;
}

export interface Address {
  id?: string;
  line1: string;
  line2?: string;
  city: string;
  region?: string;
  postalCode: string;
  country: string;
}

export interface Order {
  id: string;
  documentNo: string;
  status: OrderStatus;
  customer: CustomerSummary;
  warehouseId: string;
  warehouseName: string;
  priceListId?: string;
  currency: string;
  orderDate: string;
  promisedDate?: string;
  totalLines: Money;
  grandTotal: Money;
  lines: OrderLine[];
  statusHistory: StatusHistoryEntry[];
  billingAddress?: Address;
  shippingAddress?: Address;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderListItem {
  id: string;
  documentNo: string;
  status: OrderStatus;
  customerName: string;
  warehouseName: string;
  orderDate: string;
  grandTotal: Money;
  lineCount: number;
}

export interface Customer {
  id: string;
  code: string;
  name: string;
  email?: string;
  phone?: string;
  paymentTermsCode?: string;
  creditLimit?: Money;
  taxId?: string;
  active: boolean;
  addresses: Address[];
  contacts: Contact[];
  createdAt: string;
}

export interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  title?: string;
}

export interface Vendor {
  id: string;
  code: string;
  name: string;
  email?: string;
  phone?: string;
  paymentTermsCode?: string;
  active: boolean;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  description?: string;
  categoryId?: string;
  categoryName?: string;
  uom: string;
  listPrice?: Money;
  active: boolean;
  attributeSetId?: string;
  weight?: string;
  volume?: string;
}

export interface ProductCategory {
  id: string;
  code: string;
  name: string;
  parentId?: string;
}

export interface StockRow {
  productId: string;
  sku: string;
  productName: string;
  warehouseId: string;
  warehouseName: string;
  locatorCode?: string;
  qtyOnHand: string;
  qtyReserved: string;
  qtyAvailable: string;
  reorderPoint?: string;
  uom: string;
  isLowStock: boolean;
}

export interface StockMovement {
  id: string;
  movementDate: string;
  movementType: string;
  productSku: string;
  productName: string;
  warehouseName: string;
  locatorCode?: string;
  quantity: string;
  uom: string;
  referenceType?: string;
  referenceNo?: string;
  postedBy: string;
}

export interface Warehouse {
  id: string;
  code: string;
  name: string;
  active: boolean;
  locators: Locator[];
  address?: Address;
}

export interface Locator {
  id: string;
  code: string;
  aisle?: string;
  bin?: string;
  level?: string;
  active: boolean;
}

export interface Carrier {
  id: string;
  code: string;
  name: string;
  active: boolean;
  services: CarrierService[];
}

export interface CarrierService {
  id: string;
  code: string;
  name: string;
  transitDays?: number;
}

export interface PriceList {
  id: string;
  name: string;
  currency: string;
  isSalesPriceList: boolean;
  versions: PriceListVersion[];
}

export interface PriceListVersion {
  id: string;
  validFrom: string;
  validTo?: string;
  active: boolean;
  prices: ProductPrice[];
}

export interface ProductPrice {
  productId: string;
  productSku: string;
  productName: string;
  listPrice: Money;
  standardPrice: Money;
  limitPrice?: Money;
}

export interface TaxCategory {
  id: string;
  code: string;
  name: string;
  rates: TaxRate[];
}

export interface TaxRate {
  id: string;
  name: string;
  rate: string;
  country: string;
  region?: string;
  validFrom: string;
  validTo?: string;
}

export interface EmailTemplate {
  id: string;
  code: string;
  name: string;
  subject: string;
  bodyHtml: string;
  bodyText?: string;
  variables: string[];
  updatedAt: string;
}

export interface Receipt {
  id: string;
  documentNo: string;
  status: DocumentStatus;
  vendorName: string;
  warehouseName: string;
  receiptDate: string;
  postedAt?: string;
  lines: ReceiptLine[];
  totalQty: string;
  notes?: string;
}

export interface ReceiptLine {
  id: string;
  lineNo: number;
  productSku: string;
  productName: string;
  quantity: string;
  uom: string;
  locatorCode?: string;
  purchaseOrderRef?: string;
}

export interface PurchaseOrder {
  id: string;
  documentNo: string;
  status: DocumentStatus;
  vendorName: string;
  warehouseName: string;
  orderDate: string;
  promisedDate?: string;
  grandTotal: Money;
  lineCount: number;
  receiptDocumentNo?: string;
}

export interface Shipment {
  id: string;
  documentNo: string;
  status: DocumentStatus;
  orderDocumentNo: string;
  carrierName?: string;
  serviceCode?: string;
  trackingNumber?: string;
  shippedAt?: string;
  customerName: string;
}

export type NotificationChannel = "EMAIL" | "SMS" | "WEBHOOK" | "INTERNAL";

export type NotificationStatus =
  | "PENDING"
  | "SENT"
  | "FAILED"
  | "RETRYING"
  | "SUPPRESSED";

export interface NotificationLogEntry {
  id: string;
  occurredAt: string;
  channel: NotificationChannel;
  templateCode: string;
  recipient: string;
  subject?: string;
  status: NotificationStatus;
  attempts: number;
  errorMessage?: string;
  referenceType?: string;
  referenceId?: string;
}

export interface DashboardCounts {
  orderCount: number;
  productCount: number;
  receiptCount: number;
  shipmentCount: number;
  notificationCount: number;
}

export interface RecentOrdersBucket {
  date: string;
  count: number;
}

export interface LowStockBucket {
  warehouseName: string;
  count: number;
}

export interface SystemSettings {
  appName: string;
  version: string;
  buildSha: string;
  buildDate: string;
  environment: string;
  apiGatewayUrl: string;
  services: ServiceHealth[];
}

export interface ServiceHealth {
  name: string;
  status: "UP" | "DOWN" | "DEGRADED" | "UNKNOWN";
  url?: string;
  notes?: string;
}
