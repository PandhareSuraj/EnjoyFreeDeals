import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

type AffiliateSource = {
  id: string;
  store_name: string;
  source_type: string;
  api_url: string;
  affiliate_tag: string;
  auth_secret_name?: string;
  field_mapping?: Record<string, string>;
  min_discount_percent?: number;
  only_near_lowest?: boolean;
  is_active: boolean;
};

type NormalizedDeal = {
  deal_id: string;
  external_product_id: string;
  title: string;
  description: string;
  product_image: string;
  original_price: number;
  current_price: number;
  store_name: string;
  store_logo: string;
  category_id: string;
  deal_type: string;
  coupon_code: string;
  cashback_text: string;
  deal_url: string;
  affiliate_url: string;
  target_url: string;
  source_platform: string;
  source_type: string;
  is_hot_deal: boolean;
  is_free_deal: boolean;
  is_verified: boolean;
  availability: "in_stock" | "out_of_stock" | "unknown";
  lowest_price?: number;
  highest_price?: number;
  price_history?: number[];
  expiry_date?: number;
};

const DEFAULT_FIELD_MAPPING: Record<string, string> = {
  external_product_id: "id",
  title: "title",
  description: "description",
  product_image: "image",
  original_price: "original_price",
  current_price: "current_price",
  lowest_price: "lowest_price",
  highest_price: "highest_price",
  deal_url: "deal_url",
  affiliate_url: "affiliate_url",
  target_url: "target_url",
  category_id: "category_id",
  coupon_code: "coupon_code",
  cashback_text: "cashback_text",
  availability: "availability",
  expiry_date: "expiry_date",
  is_hot_deal: "is_hot_deal",
  is_free_deal: "is_free_deal",
};

serve(async () => {
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!supabaseUrl || !serviceRoleKey) {
    return jsonResponse({ error: "Missing Supabase server environment variables" }, 500);
  }

  const supabase = createClient(supabaseUrl, serviceRoleKey);
  const { data: sources, error: sourceError } = await supabase
    .from("affiliate_sources")
    .select("*")
    .eq("is_active", true);

  if (sourceError) return jsonResponse({ error: sourceError.message }, 500);

  const normalizedDeals = await fetchAllDeals((sources ?? []) as AffiliateSource[]);
  const results = [];

  for (const deal of normalizedDeals) {
    const result = await upsertLiveDeal(supabase, deal);
    results.push(result);
  }

  return jsonResponse({ processed: results.length, results });
});

async function fetchAllDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  const dealGroups = await Promise.all(sources.map(fetchDealsFromSource));
  return dealGroups
    .flat()
    .filter((deal) => isExactOfferUrl(firstAvailableUrl(deal)));
}

async function fetchDealsFromSource(source: AffiliateSource): Promise<NormalizedDeal[]> {
  if (!source.api_url) return [];

  const headers = new Headers({ accept: "application/json" });
  const token = source.auth_secret_name ? Deno.env.get(source.auth_secret_name) : undefined;
  if (token) headers.set("authorization", token.startsWith("Bearer ") ? token : `Bearer ${token}`);

  const response = await fetch(source.api_url, { headers });
  if (!response.ok) {
    console.error(`Source ${source.store_name} failed: ${response.status} ${response.statusText}`);
    return [];
  }

  const payload = await response.json();
  return extractFeedItems(payload)
    .map((item, index) => normalizeFeedItem(source, item, index))
    .filter((deal): deal is NormalizedDeal => Boolean(deal))
    .filter((deal) => passesSourceQuality(source, deal));
}

function extractFeedItems(payload: unknown): Record<string, unknown>[] {
  if (Array.isArray(payload)) return payload.filter(isRecord);
  if (!isRecord(payload)) return [];
  const candidates = [payload.items, payload.products, payload.deals, payload.data, payload.results];
  const array = candidates.find(Array.isArray);
  return Array.isArray(array) ? array.filter(isRecord) : [];
}

function normalizeFeedItem(source: AffiliateSource, item: Record<string, unknown>, index: number): NormalizedDeal | null {
  const mapping = { ...DEFAULT_FIELD_MAPPING, ...(source.field_mapping ?? {}) };
  const externalProductId = stringValue(readMapped(item, mapping.external_product_id)) || `${source.store_name}-${index}`;
  const title = stringValue(readMapped(item, mapping.title));
  const currentPrice = numberValue(readMapped(item, mapping.current_price));
  const originalPrice = numberValue(readMapped(item, mapping.original_price)) || currentPrice;
  const rawIsFreeDeal = booleanValue(readMapped(item, mapping.is_free_deal)) || currentPrice === 0;
  const dealUrl = stringValue(readMapped(item, mapping.deal_url));
  const affiliateUrl = stringValue(readMapped(item, mapping.affiliate_url));
  const targetUrl = stringValue(readMapped(item, mapping.target_url));
  if (!title || (!rawIsFreeDeal && currentPrice <= 0) || !firstNonBlank(affiliateUrl, dealUrl, targetUrl)) return null;

  const lowestPrice = numberValue(readMapped(item, mapping.lowest_price));
  const highestPrice = numberValue(readMapped(item, mapping.highest_price));
  const expiryDate = timestampMillis(readMapped(item, mapping.expiry_date));
  const availability = availabilityValue(readMapped(item, mapping.availability));

  return {
    deal_id: stableDealId(source.store_name, externalProductId),
    external_product_id: externalProductId,
    title,
    description: stringValue(readMapped(item, mapping.description)),
    product_image: stringValue(readMapped(item, mapping.product_image)),
    original_price: originalPrice,
    current_price: currentPrice,
    lowest_price: lowestPrice || undefined,
    highest_price: highestPrice || undefined,
    price_history: [originalPrice, currentPrice].filter((price) => price > 0),
    store_name: source.store_name,
    store_logo: "",
    category_id: stringValue(readMapped(item, mapping.category_id)) || "electronics",
    deal_type: currentPrice === 0 ? "Free Deal" : "Discount",
    coupon_code: stringValue(readMapped(item, mapping.coupon_code)),
    cashback_text: stringValue(readMapped(item, mapping.cashback_text)),
    deal_url: dealUrl,
    affiliate_url: affiliateUrl || generateAffiliateUrlFromSource(source, dealUrl || targetUrl),
    target_url: targetUrl,
    source_platform: source.store_name,
    source_type: source.source_type || "Live Affiliate API",
    is_hot_deal: booleanValue(readMapped(item, mapping.is_hot_deal)),
    is_free_deal: rawIsFreeDeal,
    is_verified: true,
    availability,
    expiry_date: expiryDate || undefined,
  };
}

async function upsertLiveDeal(supabase: ReturnType<typeof createClient>, deal: NormalizedDeal) {
  const now = new Date().toISOString();
  const exactUrl = firstAvailableUrl(deal);
  if (!isExactOfferUrl(exactUrl)) {
    return { deal_id: deal.deal_id, ok: false, skipped: true, reason: "Missing exact product or affiliate URL" };
  }

  const currentPrice = Math.max(0, deal.current_price);
  const originalPrice = Math.max(deal.original_price, currentPrice);
  const discountPercent = originalPrice > 0
    ? Math.max(0, Math.round(((originalPrice - currentPrice) / originalPrice) * 100))
    : 0;

  let query = supabase
    .from("deals")
    .select("deal_id, lowest_price, highest_price, price_history")
    .limit(1);
  query = deal.external_product_id
    ? query.or(`deal_id.eq.${deal.deal_id},external_product_id.eq.${deal.external_product_id}`)
    : query.eq("deal_id", deal.deal_id);
  const { data: existingRows } = await query;

  const existing = existingRows?.[0];
  const previousHistory = Array.isArray(existing?.price_history) ? existing.price_history : [];
  const sourceHistory = Array.isArray(deal.price_history) ? deal.price_history : [];
  const priceHistory = [...previousHistory, ...sourceHistory, currentPrice]
    .slice(-100);
  const historicPrices = priceHistory.map((entry) => Number(entry)).filter((price) => price >= 0);
  const lowestPrice = Math.min(...historicPrices, deal.lowest_price ?? currentPrice);
  const highestPrice = Math.max(...historicPrices, deal.highest_price ?? originalPrice);
  if (!passesDealQuality(currentPrice, lowestPrice, discountPercent, deal)) {
    return { deal_id: deal.deal_id, ok: false, skipped: true, reason: "Deal is not near lowest price, 60% off, free, or hot" };
  }

  const row = {
    ...deal,
    affiliate_url: deal.affiliate_url || generateAffiliateUrl(deal),
    discounted_price: currentPrice,
    current_price: currentPrice,
    original_price: originalPrice,
    lowest_price: lowestPrice,
    highest_price: highestPrice,
    price_history: priceHistory,
    discount_percent: discountPercent,
    is_lowest_price_now: currentPrice <= lowestPrice,
    is_active: deal.availability === "in_stock" && !isExpired(deal.expiry_date),
    availability: isExpired(deal.expiry_date) ? "out_of_stock" : deal.availability,
    last_price_checked_at: now,
    updated_at: now,
    source_type: deal.source_type || "Live Affiliate API",
  };

  const { error: upsertError } = await supabase
    .from("deals")
    .upsert(row, { onConflict: "deal_id" });

  if (upsertError) return { deal_id: deal.deal_id, ok: false, error: upsertError.message };

  await supabase.from("deal_price_history").insert({
    deal_id: deal.deal_id,
    price: currentPrice,
    original_price: originalPrice,
    source_url: deal.deal_url || deal.target_url,
  });

  return { deal_id: deal.deal_id, ok: true };
}

function generateAffiliateUrl(deal: NormalizedDeal): string {
  const exactUrl = deal.deal_url || deal.target_url;
  if (!isExactOfferUrl(exactUrl)) return "";
  const encoded = encodeURIComponent(exactUrl);
  return `https://YOUR_AFFILIATE_REDIRECT_DOMAIN/offer?store=${encodeURIComponent(deal.store_name)}&url=${encoded}`;
}

function generateAffiliateUrlFromSource(source: AffiliateSource, exactUrl: string): string {
  if (!source.affiliate_tag || !isExactOfferUrl(exactUrl)) return "";
  const encoded = encodeURIComponent(exactUrl);
  return `https://YOUR_AFFILIATE_REDIRECT_DOMAIN/offer?store=${encodeURIComponent(source.store_name)}&tag=${encodeURIComponent(source.affiliate_tag)}&url=${encoded}`;
}

function firstAvailableUrl(deal: NormalizedDeal): string {
  return normalizeUrl(deal.affiliate_url || deal.deal_url || deal.target_url || "");
}

function normalizeUrl(rawUrl: string): string {
  const trimmed = rawUrl.trim();
  if (!trimmed) return "";
  return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
}

function isExpired(expiryDate?: number): boolean {
  return Boolean(expiryDate && expiryDate > 0 && expiryDate <= Date.now());
}

function isExactOfferUrl(rawUrl: string): boolean {
  const normalized = normalizeUrl(rawUrl).toLowerCase();
  if (!normalized) return false;
  if (["mock", "placeholder", "dummy", "example.com", "enjoyfreedeals.example"].some((token) => normalized.includes(token))) return false;

  let url: URL;
  try {
    url = new URL(normalized);
  } catch {
    return false;
  }

  const host = url.hostname.replace(/^www\./, "");
  const path = url.pathname.replace(/^\/|\/$/g, "");
  const query = url.search.toLowerCase();
  const nestedOfferUrl = url.searchParams.get("url");
  if (nestedOfferUrl && normalizeUrl(nestedOfferUrl).toLowerCase() !== normalized) {
    return isExactOfferUrl(nestedOfferUrl);
  }
  if (!path) return false;

  const listingPaths = new Set(["deals", "offers", "sale", "coupon", "coupons", "category", "categories", "shop", "collections", "c", "s", "search", "store", "stores"]);
  if (listingPaths.has(path.toLowerCase())) return false;

  const searchSignals = ["search", "s?k=", "q=", "query=", "keyword=", "text=", "searchb", "searchtext="];
  if (searchSignals.some((signal) => path.toLowerCase().includes(signal.replace("=", "")) || query.includes(signal))) return false;

  if (host.includes("amazon.")) return /\/(dp|gp\/product)\/[a-z0-9]{6,}/i.test(`/${path}`);
  if (host.includes("flipkart.")) return `/${path}/`.includes("/p/");
  if (host.includes("meesho.")) return `/${path}/`.includes("/p/");
  if (host.includes("myntra.")) return path.split("/").length >= 2 && /\d/.test(path);
  if (host.includes("ajio.")) return `/${path}/`.includes("/p/");
  if (host.includes("tatacliq.")) return /\/p-[a-z0-9-]+/i.test(`/${path}`);
  if (host.includes("nykaa.")) return `/${path}/`.includes("/p/") || /\d/.test(path);
  if (host.includes("croma.")) return `/${path}/`.includes("/p/");
  if (host.includes("jiomart.")) return `/${path}/`.includes("/p/");
  if (host.includes("bigbasket.")) return `/${path}/`.includes("/pd/");

  return path.split("/").some((segment) => /\d/.test(segment) || segment.length >= 12);
}

function passesSourceQuality(source: AffiliateSource, deal: NormalizedDeal): boolean {
  const currentPrice = Math.max(0, deal.current_price);
  const originalPrice = Math.max(deal.original_price, currentPrice);
  const discountPercent = originalPrice > 0 ? Math.max(0, Math.round(((originalPrice - currentPrice) / originalPrice) * 100)) : 0;
  const lowestPrice = deal.lowest_price && deal.lowest_price > 0 ? deal.lowest_price : currentPrice;
  const minDiscount = source.min_discount_percent ?? 60;
  const nearLowest = currentPrice > 0 && lowestPrice > 0 && currentPrice <= lowestPrice * 1.10;
  if (source.only_near_lowest) return nearLowest || deal.is_free_deal || deal.is_hot_deal;
  return nearLowest || discountPercent >= minDiscount || deal.is_free_deal || deal.is_hot_deal;
}

function passesDealQuality(currentPrice: number, lowestPrice: number, discountPercent: number, deal: NormalizedDeal): boolean {
  const nearLowest = currentPrice > 0 && lowestPrice > 0 && currentPrice <= lowestPrice * 1.10;
  return nearLowest || discountPercent >= 60 || deal.is_free_deal || deal.is_hot_deal;
}

function readMapped(item: Record<string, unknown>, path?: string): unknown {
  if (!path) return undefined;
  return path.split(".").reduce<unknown>((value, key) => isRecord(value) ? value[key] : undefined, item);
}

function stringValue(value: unknown): string {
  return typeof value === "string" ? value.trim() : value == null ? "" : String(value).trim();
}

function numberValue(value: unknown): number {
  if (typeof value === "number") return Number.isFinite(value) ? value : 0;
  if (typeof value !== "string") return 0;
  const parsed = Number(value.replace(/[^\d.]/g, ""));
  return Number.isFinite(parsed) ? parsed : 0;
}

function booleanValue(value: unknown): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") return ["true", "1", "yes", "y"].includes(value.toLowerCase());
  return false;
}

function availabilityValue(value: unknown): "in_stock" | "out_of_stock" | "unknown" {
  const normalized = stringValue(value).toLowerCase().replace(/\s+/g, "_");
  if (!normalized) return "in_stock";
  if (["in_stock", "instock", "available", "true"].includes(normalized)) return "in_stock";
  if (["out_of_stock", "outofstock", "unavailable", "false"].includes(normalized)) return "out_of_stock";
  return "unknown";
}

function timestampMillis(value: unknown): number {
  if (typeof value === "number") return value > 10_000_000_000 ? value : value * 1000;
  const text = stringValue(value);
  if (!text) return 0;
  const parsed = Date.parse(text);
  return Number.isFinite(parsed) ? parsed : 0;
}

function firstNonBlank(...values: string[]): string {
  return values.find((value) => value.trim().length > 0) ?? "";
}

function stableDealId(storeName: string, externalProductId: string): string {
  return `${storeName}-${externalProductId}`
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body, null, 2), {
    status,
    headers: { "content-type": "application/json" },
  });
}
