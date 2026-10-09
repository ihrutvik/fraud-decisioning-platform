create table decision_feedback (
  id uuid primary key,
  tenant_id varchar(80) not null,
  decision_id uuid not null references transaction_decisions(id),
  idempotency_key varchar(100) not null,
  request_fingerprint varchar(64) not null,
  label varchar(24) not null check (label in ('CONFIRMED_FRAUD','CONFIRMED_LEGIT','INCONCLUSIVE')),
  source varchar(24) not null check (source in ('CHARGEBACK','MANUAL_REVIEW','CUSTOMER_REPORT','PAYMENT_NETWORK')),
  external_reference varchar(160) not null,
  observed_at timestamptz not null,
  created_at timestamptz not null,
  constraint uk_feedback_tenant_key unique (tenant_id,idempotency_key),
  constraint uk_feedback_source_reference unique (tenant_id,source,external_reference)
);
create index idx_feedback_decision_observed on decision_feedback(tenant_id,decision_id,observed_at);
create index idx_feedback_training_export on decision_feedback(label,created_at);
