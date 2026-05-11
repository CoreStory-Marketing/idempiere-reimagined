/**
 * Mock data factories — only invoked when MOCK_ENABLED=true in dev mode.
 * The demo path uses real backend data; these exist so pages render
 * during local UI work when the backend is down.
 */

import type {
  Carrier,
  Customer,
  DashboardCounts,
  EmailTemplate,
  LowStockBucket,
  NotificationLogEntry,
  Order,
  OrderListItem,
  OrderStatus,
  PageResponse,
  PriceList,
  Product,
  PurchaseOrder,
  Receipt,
  RecentOrdersBucket,
  Shipment,
  StockMovement,
  StockRow,
  SystemSettings,
  TaxCategory,
  Vendor,
  Warehouse,
} from "@/lib/types/domain";

const STATUSES: OrderStatus[] = [
  "DRAFT",
  "PENDING",
  "CONFIRMED",
  "SHIPPED",
  "INVOICED",
  "COMPLETE",
  "CANCELLED",
];

function pad(n: number, width = 5): string {
  return n.toString().padStart(width, "0");
}

function isoDaysAgo(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString();
}

function pickStatus(i: number): OrderStatus {
  const s = STATUSES[i % STATUSES.length];
  return s ?? "DRAFT";
}

export function mockOrderList(page = 0, size = 20): PageResponse<OrderListItem> {
  const total = 87;
  const start = page * size;
  const end = Math.min(start + size, total);
  const content: OrderListItem[] = [];
  for (let i = start; i < end; i++) {
    content.push({
      id: `ord-${pad(i + 1)}`,
      documentNo: `SO-${pad(1000 + i, 6)}`,
      status: pickStatus(i),
      customerName: `Customer ${String.fromCharCode(65 + (i % 26))} & Co.`,
      warehouseName: i % 2 === 0 ? "HQ Warehouse" : "West DC",
      orderDate: isoDaysAgo(i % 14),
      grandTotal: { amount: (200 + i * 37.5).toFixed(2), currency: "USD" },
      lineCount: (i % 6) + 1,
    });
  }
  return {
    items: content,
    page,
    pageSize: size,
    total,
    totalPages: Math.ceil(total / size),
  };
}

export function mockOrder(id: string): Order {
  const idx = Number.parseInt(id.replace(/\D/g, ""), 10) || 1;
  const status = pickStatus(idx);
  const lines = Array.from({ length: 4 }, (_, i) => ({
    id: `${id}-line-${i + 1}`,
    lineNo: i + 1,
    productId: `prod-${pad(100 + i)}`,
    productSku: `SKU-${pad(100 + i, 4)}`,
    productName: `Widget ${String.fromCharCode(65 + i)}`,
    quantity: ((i + 1) * 5).toFixed(2),
    uom: "EA",
    unitPrice: { amount: (12.5 + i * 3.25).toFixed(2), currency: "USD" },
    lineTotal: { amount: ((i + 1) * 5 * (12.5 + i * 3.25)).toFixed(2), currency: "USD" },
    description: `Standard ${String.fromCharCode(65 + i)} variant`,
  }));
  const lineSum = lines.reduce((acc, l) => acc + Number(l.lineTotal.amount), 0);
  return {
    id,
    documentNo: `SO-${pad(1000 + idx, 6)}`,
    status,
    customer: {
      id: "cust-001",
      code: "C-001",
      name: "Acme Corp",
      email: "ap@acme.example",
      phone: "+1-555-0100",
    },
    warehouseId: "wh-001",
    warehouseName: "HQ Warehouse",
    priceListId: "pl-001",
    currency: "USD",
    orderDate: isoDaysAgo(idx % 10),
    promisedDate: isoDaysAgo(-3),
    totalLines: { amount: lineSum.toFixed(2), currency: "USD" },
    grandTotal: { amount: (lineSum * 1.08).toFixed(2), currency: "USD" },
    lines,
    statusHistory: [
      { status: "DRAFT", changedAt: isoDaysAgo(5), changedBy: "admin" },
      ...(status === "DRAFT"
        ? []
        : [{ status: "CONFIRMED" as const, changedAt: isoDaysAgo(3), changedBy: "admin" }]),
      ...(["SHIPPED", "INVOICED", "COMPLETE"].includes(status)
        ? [{ status: "SHIPPED" as const, changedAt: isoDaysAgo(2), changedBy: "system" }]
        : []),
      ...(["INVOICED", "COMPLETE"].includes(status)
        ? [{ status: "INVOICED" as const, changedAt: isoDaysAgo(1), changedBy: "system" }]
        : []),
      ...(status === "COMPLETE"
        ? [{ status: "COMPLETE" as const, changedAt: isoDaysAgo(0), changedBy: "system" }]
        : []),
    ],
    billingAddress: {
      line1: "123 Market St",
      city: "San Francisco",
      region: "CA",
      postalCode: "94103",
      country: "US",
    },
    shippingAddress: {
      line1: "456 Industrial Ave",
      city: "Oakland",
      region: "CA",
      postalCode: "94607",
      country: "US",
    },
    createdAt: isoDaysAgo(5),
    updatedAt: isoDaysAgo(0),
  };
}

export function mockDashboardCounts(): DashboardCounts {
  return {
    orderCount: 87,
    productCount: 312,
    receiptCount: 24,
    shipmentCount: 0,
    notificationCount: 0,
  };
}

export function mockRecentOrders(): RecentOrdersBucket[] {
  return Array.from({ length: 7 }, (_, i) => ({
    date: isoDaysAgo(6 - i).slice(0, 10),
    count: 3 + ((i * 7) % 9),
  }));
}

export function mockLowStock(): LowStockBucket[] {
  return [
    { warehouseName: "HQ Warehouse", count: 7 },
    { warehouseName: "West DC", count: 3 },
    { warehouseName: "East DC", count: 5 },
  ];
}

export function mockStock(): StockRow[] {
  return Array.from({ length: 30 }, (_, i) => {
    const onHand = 100 - i * 3;
    const reserved = (i * 2) % 30;
    return {
      productId: `prod-${pad(100 + i)}`,
      sku: `SKU-${pad(100 + i, 4)}`,
      productName: `Widget ${String.fromCharCode(65 + (i % 26))}-${i}`,
      warehouseId: i % 2 === 0 ? "wh-001" : "wh-002",
      warehouseName: i % 2 === 0 ? "HQ Warehouse" : "West DC",
      locatorCode: `A-${pad((i % 10) + 1, 2)}-${(i % 4) + 1}`,
      qtyOnHand: String(Math.max(0, onHand)),
      qtyReserved: String(reserved),
      qtyAvailable: String(Math.max(0, onHand - reserved)),
      reorderPoint: "20",
      uom: "EA",
      isLowStock: onHand - reserved < 20,
    };
  });
}

export function mockMovements(): StockMovement[] {
  return Array.from({ length: 25 }, (_, i) => ({
    id: `mvt-${pad(i + 1)}`,
    movementDate: isoDaysAgo(i % 10),
    movementType: ["RECEIPT", "ISSUE", "TRANSFER", "ADJUSTMENT"][i % 4] ?? "RECEIPT",
    productSku: `SKU-${pad(100 + (i % 30), 4)}`,
    productName: `Widget ${String.fromCharCode(65 + (i % 26))}`,
    warehouseName: i % 2 === 0 ? "HQ Warehouse" : "West DC",
    locatorCode: `A-${pad((i % 10) + 1, 2)}-${(i % 4) + 1}`,
    quantity: String((i % 2 === 0 ? 1 : -1) * ((i % 12) + 1)),
    uom: "EA",
    referenceType: ["RECEIPT", "ORDER", "TRANSFER"][i % 3],
    referenceNo: `REF-${pad(2000 + i, 5)}`,
    postedBy: i % 3 === 0 ? "admin" : "system",
  }));
}

export function mockReceipts(): Receipt[] {
  return Array.from({ length: 6 }, (_, i) => ({
    id: `rcpt-${pad(i + 1)}`,
    documentNo: `MR-${pad(500 + i, 5)}`,
    status: i % 2 === 0 ? "COMPLETED" : "DRAFT",
    vendorName: `Supplier ${String.fromCharCode(88 - i)}`,
    warehouseName: i % 2 === 0 ? "HQ Warehouse" : "West DC",
    receiptDate: isoDaysAgo(i),
    postedAt: i % 2 === 0 ? isoDaysAgo(i) : undefined,
    lines: Array.from({ length: 3 }, (_, j) => ({
      id: `rl-${i}-${j}`,
      lineNo: j + 1,
      productSku: `SKU-${pad(200 + j, 4)}`,
      productName: `Inbound Widget ${j + 1}`,
      quantity: String(50 + j * 10),
      uom: "EA",
      locatorCode: `A-01-${j + 1}`,
      purchaseOrderRef: `PO-${pad(700 + i, 5)}`,
    })),
    totalQty: String(180 + i * 10),
  }));
}

export function mockNotificationLog(): NotificationLogEntry[] {
  // Empty by design — notifications populate live during the demo.
  return [];
}

export function mockCustomers(): Customer[] {
  return Array.from({ length: 24 }, (_, i) => ({
    id: `cust-${pad(i + 1)}`,
    code: `C-${pad(i + 1, 3)}`,
    name: `${["Acme", "Globex", "Initech", "Umbrella", "Soylent"][i % 5]} ${i + 1}`,
    email: `contact${i + 1}@example.com`,
    phone: `+1-555-01${pad(i, 2)}`,
    paymentTermsCode: ["NET30", "NET15", "COD"][i % 3],
    creditLimit: { amount: (10000 + i * 1000).toFixed(2), currency: "USD" },
    taxId: `TAX-${pad(i + 100, 5)}`,
    active: i % 7 !== 0,
    addresses: [
      {
        line1: `${100 + i} Main St`,
        city: ["San Francisco", "Oakland", "San Jose"][i % 3] ?? "San Francisco",
        region: "CA",
        postalCode: `9410${i % 10}`,
        country: "US",
      },
    ],
    contacts: [
      {
        id: `ct-${i}`,
        firstName: ["Alice", "Bob", "Carol"][i % 3] ?? "Alice",
        lastName: ["Lee", "Patel", "Smith"][i % 3] ?? "Lee",
        email: `contact${i + 1}@example.com`,
        phone: `+1-555-01${pad(i, 2)}`,
        title: ["AP Manager", "Buyer", "Operations"][i % 3],
      },
    ],
    createdAt: isoDaysAgo(40 - i),
  }));
}

export function mockProducts(): Product[] {
  return Array.from({ length: 40 }, (_, i) => ({
    id: `prod-${pad(100 + i)}`,
    sku: `SKU-${pad(100 + i, 4)}`,
    name: `Widget ${String.fromCharCode(65 + (i % 26))}-${i}`,
    description: `Standard widget variant ${i + 1}`,
    categoryId: ["cat-hw", "cat-sw", "cat-cons"][i % 3],
    categoryName: ["Hardware", "Software", "Consumables"][i % 3],
    uom: "EA",
    listPrice: { amount: (10 + i * 2.5).toFixed(2), currency: "USD" },
    active: i % 11 !== 0,
    weight: String(0.5 + (i % 5) * 0.25),
    volume: String(0.01 + (i % 4) * 0.005),
  }));
}

export function mockVendors(): Vendor[] {
  return Array.from({ length: 14 }, (_, i) => ({
    id: `vend-${pad(i + 1)}`,
    code: `V-${pad(i + 1, 3)}`,
    name: `Supplier ${String.fromCharCode(88 - (i % 10))} ${i + 1}`,
    email: `sales${i + 1}@supplier.example`,
    phone: `+1-555-09${pad(i, 2)}`,
    paymentTermsCode: ["NET30", "NET45", "NET60"][i % 3],
    active: true,
  }));
}

export function mockWarehouses(): Warehouse[] {
  return [
    {
      id: "wh-001",
      code: "HQ",
      name: "HQ Warehouse",
      active: true,
      locators: Array.from({ length: 12 }, (_, i) => ({
        id: `loc-hq-${i + 1}`,
        code: `A-${pad((i % 4) + 1, 2)}-${Math.floor(i / 4) + 1}`,
        aisle: `A${(i % 4) + 1}`,
        bin: `B${Math.floor(i / 4) + 1}`,
        level: "1",
        active: true,
      })),
      address: {
        line1: "1 Industrial Park",
        city: "San Francisco",
        region: "CA",
        postalCode: "94103",
        country: "US",
      },
    },
    {
      id: "wh-002",
      code: "WEST",
      name: "West DC",
      active: true,
      locators: Array.from({ length: 8 }, (_, i) => ({
        id: `loc-west-${i + 1}`,
        code: `B-${pad((i % 4) + 1, 2)}-${Math.floor(i / 4) + 1}`,
        active: true,
      })),
      address: {
        line1: "2 Logistics Way",
        city: "Reno",
        region: "NV",
        postalCode: "89501",
        country: "US",
      },
    },
  ];
}

export function mockCarriers(): Carrier[] {
  return [
    {
      id: "car-ups",
      code: "UPS",
      name: "United Parcel Service",
      active: true,
      services: [
        { id: "svc-ups-grd", code: "GRD", name: "Ground", transitDays: 5 },
        { id: "svc-ups-2da", code: "2DA", name: "2-Day Air", transitDays: 2 },
      ],
    },
    {
      id: "car-fdx",
      code: "FDX",
      name: "FedEx",
      active: true,
      services: [
        { id: "svc-fdx-grd", code: "GRD", name: "Ground", transitDays: 5 },
        { id: "svc-fdx-exp", code: "EXP", name: "Express Saver", transitDays: 3 },
      ],
    },
    {
      id: "car-usps",
      code: "USPS",
      name: "United States Postal Service",
      active: true,
      services: [
        { id: "svc-usps-prio", code: "PRIO", name: "Priority Mail", transitDays: 3 },
      ],
    },
  ];
}

export function mockPriceLists(): PriceList[] {
  return [
    {
      id: "pl-001",
      name: "Standard Sales (USD)",
      currency: "USD",
      isSalesPriceList: true,
      versions: [
        {
          id: "plv-001",
          validFrom: isoDaysAgo(180),
          active: true,
          prices: Array.from({ length: 8 }, (_, i) => ({
            productId: `prod-${pad(100 + i)}`,
            productSku: `SKU-${pad(100 + i, 4)}`,
            productName: `Widget ${String.fromCharCode(65 + i)}`,
            listPrice: { amount: (10 + i * 2.5).toFixed(2), currency: "USD" },
            standardPrice: { amount: (9 + i * 2.4).toFixed(2), currency: "USD" },
            limitPrice: { amount: (8 + i * 2.3).toFixed(2), currency: "USD" },
          })),
        },
      ],
    },
    {
      id: "pl-002",
      name: "Wholesale (USD)",
      currency: "USD",
      isSalesPriceList: true,
      versions: [
        {
          id: "plv-002",
          validFrom: isoDaysAgo(120),
          active: true,
          prices: [],
        },
      ],
    },
  ];
}

export function mockTaxCategories(): TaxCategory[] {
  return [
    {
      id: "tc-stand",
      code: "STD",
      name: "Standard",
      rates: [
        {
          id: "tr-ca",
          name: "California Sales Tax",
          rate: "0.0875",
          country: "US",
          region: "CA",
          validFrom: isoDaysAgo(365),
        },
        {
          id: "tr-ny",
          name: "New York Sales Tax",
          rate: "0.08875",
          country: "US",
          region: "NY",
          validFrom: isoDaysAgo(365),
        },
      ],
    },
    {
      id: "tc-exempt",
      code: "EXEMPT",
      name: "Exempt",
      rates: [],
    },
  ];
}

export function mockEmailTemplates(): EmailTemplate[] {
  return [
    {
      id: "tpl-shipped",
      code: "ORDER_SHIPPED",
      name: "Order Shipped Notification",
      subject: "Your order {{order.documentNo}} has shipped",
      bodyHtml:
        "<p>Hi {{customer.firstName}},</p><p>Your order <strong>{{order.documentNo}}</strong> has shipped via {{carrier.name}}.</p><p>Tracking: {{shipment.trackingNumber}}</p>",
      bodyText:
        "Hi {{customer.firstName}}, your order {{order.documentNo}} has shipped via {{carrier.name}}. Tracking: {{shipment.trackingNumber}}",
      variables: ["customer.firstName", "order.documentNo", "carrier.name", "shipment.trackingNumber"],
      updatedAt: isoDaysAgo(7),
    },
    {
      id: "tpl-confirmed",
      code: "ORDER_CONFIRMED",
      name: "Order Confirmation",
      subject: "Order {{order.documentNo}} confirmed",
      bodyHtml: "<p>Thanks for your order!</p>",
      variables: ["customer.firstName", "order.documentNo"],
      updatedAt: isoDaysAgo(20),
    },
  ];
}

export function mockPurchaseOrders(): PurchaseOrder[] {
  return Array.from({ length: 12 }, (_, i) => ({
    id: `po-${pad(i + 1)}`,
    documentNo: `PO-${pad(700 + i, 5)}`,
    status: i % 2 === 0 ? "COMPLETED" : "IN_PROGRESS",
    vendorName: `Supplier ${String.fromCharCode(88 - (i % 10))}`,
    warehouseName: i % 2 === 0 ? "HQ Warehouse" : "West DC",
    orderDate: isoDaysAgo(i + 5),
    promisedDate: isoDaysAgo(-(i + 2)),
    grandTotal: { amount: (5000 + i * 300).toFixed(2), currency: "USD" },
    lineCount: (i % 5) + 1,
    receiptDocumentNo: i % 2 === 0 ? `MR-${pad(500 + i, 5)}` : undefined,
  }));
}

export function mockShipments(): Shipment[] {
  // Empty until shipping-service lands during the demo.
  return [];
}

export function mockSettings(): SystemSettings {
  return {
    appName: "iDempiere Reimagined",
    version: "0.1.0-SNAPSHOT",
    buildSha: "0e9c9d3",
    buildDate: isoDaysAgo(1),
    environment: "development",
    apiGatewayUrl: "http://api-gateway:8080",
    services: [
      { name: "orders-service", status: "UP", url: "http://orders-service:8081" },
      { name: "inventory-service", status: "UP", url: "http://inventory-service:8082" },
      { name: "warehouse-service", status: "UP", url: "http://warehouse-service:8083" },
      {
        name: "shipping-service",
        status: "DEGRADED",
        url: "http://shipping-service:8084",
        notes: "Stub — pending SHIP-101",
      },
      { name: "notifications-service", status: "UP", url: "http://notifications-service:8085" },
    ],
  };
}
