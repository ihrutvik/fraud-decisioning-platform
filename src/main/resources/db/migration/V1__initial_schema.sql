create table transaction_decisions (
  id uuid primary key, tenant_id varchar(80) not null, idempotency_key varchar(100) not null,
  request_fingerprint varchar(64) not null, transaction_id varchar(100) not null,
  amount numeric(19,4) not null check (amount > 0), currency varchar(3) not null,
  account_id varchar(100) not null, outcome varchar(16) not null, score integer not null check (score between 0 and 100),
  reason_codes text not null, created_at timestamptz not null,
  constraint uk_tenant_idempotency unique (tenant_id,idempotency_key)
);
create index idx_decisions_tenant_created on transaction_decisions(tenant_id,created_at desc);
create table outbox_events (
  id uuid primary key, aggregate_id uuid not null, event_type varchar(80) not null,
  payload text not null, created_at timestamptz not null, published_at timestamptz null
);
create index idx_outbox_unpublished on outbox_events(created_at) where published_at is null;
