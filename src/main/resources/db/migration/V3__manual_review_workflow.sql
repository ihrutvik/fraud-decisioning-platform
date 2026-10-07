create table review_cases (
  id uuid primary key,
  tenant_id varchar(80) not null,
  decision_id uuid not null references transaction_decisions(id),
  status varchar(16) not null check (status in ('OPEN','CLAIMED','RESOLVED')),
  resolution varchar(16) null check (resolution in ('APPROVE','DECLINE')),
  assigned_to varchar(100), lease_until timestamptz, analyst_note varchar(1000),
  created_at timestamptz not null, resolved_at timestamptz, version bigint not null default 0,
  constraint uk_review_decision unique (decision_id),
  constraint ck_review_resolution check ((status='RESOLVED' and resolution is not null and resolved_at is not null) or (status<>'RESOLVED' and resolution is null and resolved_at is null))
);
create index idx_review_queue on review_cases(tenant_id,status,created_at);
create table review_audit_events (
  id uuid primary key, review_id uuid not null references review_cases(id),
  event_type varchar(40) not null, actor_id varchar(100) not null, created_at timestamptz not null
);
create index idx_review_audit on review_audit_events(review_id,created_at);
