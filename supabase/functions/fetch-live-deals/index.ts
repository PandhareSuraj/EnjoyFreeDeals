import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

type AffiliateSource = {
  id: string;
  store_name: string;
  source_type: string;
  api_url: string;
  affiliate_tag: string;
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
  const configuredStores = new Set(sources.map((source) => source.store_name.toLowerCase()));
  const dealGroups = await Promise.all([
    fetchAmazonDeals(sources),
    fetchFlipkartDeals(sources),
    fetchMeeshoDeals(sources),
    fetchMyntraDeals(sources),
    fetchAjioDeals(sources),
    fetchTataCliqDeals(sources),
    fetchCromaDeals(sources),
    fetchNykaaDeals(sources),
  ]);
  return dealGroups
    .flat()
    .filter((deal) => configuredStores.size === 0 || configuredStores.has(deal.store_name.toLowerCase()));
}

async function upsertLiveDeal(supabase: ReturnType<typeof createClient>, deal: NormalizedDeal) {
  const now = new Date().toISOString();
  const currentPrice = Math.max(0, deal.current_price);
  const originalPrice = Math.max(deal.original_price, currentPrice);
  const discountPercent = originalPrice > 0
    ? Math.max(0, Math.round(((originalPrice - currentPrice) / originalPrice) * 100))
    : 0;

  const { data: existingRows } = await supabase
    .from("deals")
    .select("deal_id, lowest_price, highest_price, price_history")
    .eq("deal_id", deal.deal_id)
    .limit(1);

  const existing = existingRows?.[0];
  const previousHistory = Array.isArray(existing?.price_history) ? existing.price_history : [];
  const priceHistory = [...previousHistory, currentPrice]
    .slice(-100);
  const historicPrices = priceHistory.map((entry) => Number(entry)).filter((price) => price >= 0);
  const lowestPrice = Math.min(...historicPrices, currentPrice);
  const highestPrice = Math.max(...historicPrices, originalPrice);

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
    is_active: deal.availability === "in_stock",
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
  if (!exactUrl) return "";
  const encoded = encodeURIComponent(exactUrl);
  return `https://YOUR_AFFILIATE_REDIRECT_DOMAIN/offer?store=${encodeURIComponent(deal.store_name)}&url=${encoded}`;
}

async function fetchAmazonDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Amazon", sources);
}

async function fetchFlipkartDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Flipkart", sources);
}

async function fetchMeeshoDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Meesho", sources);
}

async function fetchMyntraDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Myntra", sources);
}

async function fetchAjioDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Ajio", sources);
}

async function fetchTataCliqDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("TataCliq", sources);
}

async function fetchCromaDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Croma", sources);
}

async function fetchNykaaDeals(sources: AffiliateSource[]): Promise<NormalizedDeal[]> {
  return placeholderDealsForStore("Nykaa", sources);
}

function placeholderDealsForStore(storeName: string, sources: AffiliateSource[]): NormalizedDeal[] {
  const source = sources.find((item) => item.store_name.toLowerCase() === storeName.toLowerCase());
  if (!source) return [];
  return [];
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body, null, 2), {
    status,
    headers: { "content-type": "application/json" },
  });
}
