create table shadow_evaluations (
  id uuid primary key,
  tenant_id varchar(80) not null,
  decision_id uuid not null references transaction_decisions(id),
  model_version varchar(80) not null,
  shadow_score integer not null check (shadow_score between 0 and 100),
  shadow_outcome varchar(16) not null check (shadow_outcome in ('APPROVE','REVIEW','DECLINE')),
  production_outcome varchar(16) not null check (production_outcome in ('APPROVE','REVIEW','DECLINE')),
  disagreed boolean not null,
  signals text not null,
  created_at timestamptz not null,
  constraint uk_shadow_decision_version unique (decision_id,model_version)
);
create index idx_shadow_tenant_created on shadow_evaluations(tenant_id,created_at desc);
create index idx_shadow_disagreements on shadow_evaluations(model_version,created_at desc) where disagreed;
