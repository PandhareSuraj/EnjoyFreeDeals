# EnjoyFreeDeals Live Deal Backend Plan

Important correction:
Do not try to fetch live prices directly inside Android using website scraping. Build the live fetching system through Supabase Edge Functions/backend worker, then Android reads from Supabase Realtime. The app should show current_price from Supabase, not old discounted_price from MockData.

## Final Data Flow

E-commerce affiliate APIs, price tracking APIs, approved public deal APIs, WordPress REST API, or a server scraper that is allowed by the website terms feed a Supabase Edge Function or backend worker. That worker normalizes deal data, calculates price quality, writes rows into Supabase, and the Android app listens to Supabase Realtime.

Android responsibilities:
- Read normalized live deals from Supabase.
- Display `current_price`, `lowest_price`, price history, and verification status.
- Open `affiliate_url`, then `deal_url`, then `target_url`.
- Show local notifications for new rows received through Realtime.

Android must not:
- Scrape ecommerce HTML.
- Store affiliate API secrets.
- Store Supabase service-role keys.

## Approved Source Strategy

Use these source types, in priority order:
- Amazon Product Advertising API or approved affiliate feed.
- Flipkart affiliate API or approved partner feed.
- Meesho, Ajio, Myntra, TataCliq, Croma, Nykaa approved affiliate APIs or partner feeds.
- Price tracking APIs with terms that allow backend use.
- WordPress REST API if deals are published on your own deals website.
- Custom server scraper only when the target website terms explicitly allow it.

## Supabase Worker Schedule

Run `fetch-live-deals`:
- Every 15 minutes for hot/trending products.
- Every 1 hour for normal deals.

Each run should:
- Read active `affiliate_sources`.
- Fetch product title, image, MRP, current price, availability, category, store, and exact offer URL.
- Generate `affiliate_url` using your affiliate redirect system.
- Upsert into `deals`.
- Insert every observed price into `deal_price_history`.
- Recalculate `lowest_price`, `highest_price`, `discount_percent`, and `is_lowest_price_now`.
- Set `last_price_checked_at` and `updated_at`.
- Mark unavailable products as `availability = 'out_of_stock'` or `is_active = false`.

## Android Display Rule

Show only good deals:
- `is_active = true`
- `availability = 'in_stock'`
- exact `affiliate_url` or `deal_url` or `target_url` exists
- `current_price > 0` unless it is a valid free deal
- high discount, hot/free, current price at lowest, or current price within 10% of lowest

MockData remains only a development fallback when Supabase is not configured or the network request fails.
