create extension if not exists pgcrypto;

create table if not exists public.users (
  user_id uuid primary key references auth.users(id) on delete cascade,
  name text not null default '',
  email text not null default '',
  mobile text not null default '',
  profile_image text not null default '',
  saved_deals text[] not null default '{}',
  clicked_deals text[] not null default '{}',
  shared_deals integer not null default 0,
  notification_enabled boolean not null default true,
  dark_mode_enabled boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists public.deals (
  deal_id text primary key,
  external_product_id text,
  title text not null,
  description text,
  product_image text,
  original_price numeric,
  discounted_price numeric,
  current_price numeric not null,
  lowest_price numeric,
  highest_price numeric,
  discount_percent int,
  store_name text,
  store_logo text,
  category_id text,
  deal_type text,
  coupon_code text,
  cashback_text text,
  price_history jsonb not null default '[]'::jsonb,
  is_lowest_price_now boolean default false,
  deal_url text,
  affiliate_url text,
  target_url text,
  source_platform text,
  source_type text default 'Live Affiliate API',
  is_hot_deal boolean default false,
  is_free_deal boolean default false,
  is_verified boolean default true,
  is_active boolean default true,
  availability text default 'in_stock',
  click_count int default 0,
  save_count int default 0,
  share_count int default 0,
  expiry_date bigint,
  last_price_checked_at timestamptz,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create table if not exists public.deal_price_history (
  id uuid primary key default gen_random_uuid(),
  deal_id text references public.deals(deal_id) on delete cascade,
  price numeric not null,
  original_price numeric,
  checked_at timestamptz default now(),
  source_url text
);

create table if not exists public.affiliate_sources (
  id uuid primary key default gen_random_uuid(),
  store_name text not null,
  source_type text not null,
  api_url text,
  affiliate_tag text,
  auth_secret_name text,
  field_mapping jsonb not null default '{}'::jsonb,
  min_discount_percent int not null default 60,
  only_near_lowest boolean not null default false,
  is_active boolean default true,
  last_synced_at timestamptz
);

alter table public.affiliate_sources add column if not exists auth_secret_name text;
alter table public.affiliate_sources add column if not exists field_mapping jsonb not null default '{}'::jsonb;
alter table public.affiliate_sources add column if not exists min_discount_percent int not null default 60;
alter table public.affiliate_sources add column if not exists only_near_lowest boolean not null default false;

comment on table public.affiliate_sources is
'Approved affiliate/API source configuration. Android must not scrape ecommerce sites; backend workers use these sources and write normalized exact-offer rows.';

comment on column public.deals.affiliate_url is
'Exact affiliate/product offer URL. Do not store home, search, category, mock, or placeholder URLs.';

create table if not exists public.categories (
  category_id text primary key,
  name text not null,
  icon text not null default '',
  category_image text not null default '',
  gradient_color_1 text not null default '#E91B23',
  gradient_color_2 text not null default '#006B2E',
  is_active boolean not null default true
);

create table if not exists public.blogs (
  blog_id text primary key,
  title text not null,
  image text not null default '',
  summary text not null default '',
  content text not null default '',
  created_at timestamptz not null default now()
);

create table if not exists public.notifications (
  notification_id text primary key,
  title text not null,
  message text not null default '',
  image text not null default '',
  deal_id text not null default '',
  target_url text not null default '',
  is_read boolean not null default false,
  created_at timestamptz not null default now()
);

alter table public.users enable row level security;
alter table public.deals enable row level security;
alter table public.deal_price_history enable row level security;
alter table public.affiliate_sources enable row level security;
alter table public.categories enable row level security;
alter table public.blogs enable row level security;
alter table public.notifications enable row level security;

drop policy if exists "Public can read active deals" on public.deals;
create policy "Public can read active deals" on public.deals
for select using (is_active = true);

drop policy if exists "Public can read deal price history" on public.deal_price_history;
create policy "Public can read deal price history" on public.deal_price_history
for select using (true);

drop policy if exists "Public can read active categories" on public.categories;
create policy "Public can read active categories" on public.categories
for select using (is_active = true);

drop policy if exists "Public can read blogs" on public.blogs;
create policy "Public can read blogs" on public.blogs
for select using (true);

drop policy if exists "Public can read notifications" on public.notifications;
create policy "Public can read notifications" on public.notifications
for select using (true);

drop policy if exists "Users can read own profile" on public.users;
create policy "Users can read own profile" on public.users
for select using (auth.uid() = user_id);

drop policy if exists "Users can insert own profile" on public.users;
create policy "Users can insert own profile" on public.users
for insert with check (auth.uid() = user_id);

drop policy if exists "Users can update own profile" on public.users;
create policy "Users can update own profile" on public.users
for update using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "Service role manages affiliate sources" on public.affiliate_sources;
create policy "Service role manages affiliate sources" on public.affiliate_sources
for all using (auth.role() = 'service_role') with check (auth.role() = 'service_role');

insert into public.categories (category_id, name, icon, gradient_color_1, gradient_color_2) values
('electronics', 'Electronics', 'Bolt', '#E91B23', '#FF8A00'),
('mobiles', 'Mobiles', 'Phone', '#006B2E', '#21B573'),
('fashion', 'Fashion', 'Shirt', '#E91B23', '#7C3AED'),
('beauty', 'Beauty', 'Sparkles', '#D946EF', '#F97316'),
('grocery', 'Grocery', 'Basket', '#006B2E', '#FFD600'),
('coupons', 'Coupons', 'Badge', '#111827', '#E91B23'),
('bank', 'Bank Offers', 'Card', '#1E1E1E', '#006B2E')
on conflict (category_id) do nothing;

insert into public.affiliate_sources (
  store_name, source_type, api_url, affiliate_tag, auth_secret_name,
  field_mapping, min_discount_percent, only_near_lowest, is_active
) values
('Amazon', 'affiliate_api', null, 'YOUR_AMAZON_TAG', 'AMAZON_FEED_TOKEN', '{}'::jsonb, 60, false, false),
('Flipkart', 'affiliate_api', null, 'YOUR_FLIPKART_TAG', 'FLIPKART_FEED_TOKEN', '{}'::jsonb, 60, false, false),
('Myntra', 'affiliate_api', null, 'YOUR_MYNTRA_TAG', 'MYNTRA_FEED_TOKEN', '{}'::jsonb, 60, false, false),
('Ajio', 'affiliate_api', null, 'YOUR_AJIO_TAG', 'AJIO_FEED_TOKEN', '{}'::jsonb, 60, false, false),
('Croma', 'affiliate_api', null, 'YOUR_CROMA_TAG', 'CROMA_FEED_TOKEN', '{}'::jsonb, 60, false, false),
('Nykaa', 'affiliate_api', null, 'YOUR_NYKAA_TAG', 'NYKAA_FEED_TOKEN', '{}'::jsonb, 60, false, false)
on conflict do nothing;

insert into public.deals (
  deal_id, external_product_id, title, description, product_image, original_price,
  discounted_price, current_price, lowest_price, highest_price, discount_percent,
  store_name, category_id, deal_type, price_history, deal_url, source_platform,
  source_type, is_hot_deal, is_free_deal, is_verified, is_active, availability, expiry_date,
  last_price_checked_at
) values
('amazon-boat-earbuds', 'amazon-boat-earbuds-demo', 'Amazon - boAt Earbuds', 'Demo inactive row. Replace with approved affiliate API data before publishing.', '', 2999, 1199, 1199, 1199, 2999, 60, 'Amazon', 'electronics', 'Discount', '[2999,1899,1199]', '', 'Amazon', 'Demo - URL unavailable', true, false, true, false, 'out_of_stock', 0, now()),
('flipkart-realme-phone', 'flipkart-realme-phone-demo', 'Flipkart - Realme Smartphone', 'Demo inactive row. Replace with approved affiliate API data before publishing.', '', 14999, 11999, 11999, 11999, 14999, 20, 'Flipkart', 'mobiles', 'Discount', '[14999,12999,11999]', '', 'Flipkart', 'Demo - URL unavailable', false, false, true, false, 'out_of_stock', 0, now())
on conflict (deal_id) do nothing;
