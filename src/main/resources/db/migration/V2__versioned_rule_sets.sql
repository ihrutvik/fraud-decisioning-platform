create table fraud_rule_sets (
  id uuid primary key,
  tenant_id varchar(80) not null,
  rule_version bigint not null,
  status varchar(16) not null check (status in ('DRAFT','ACTIVE','RETIRED')),
  high_value_threshold integer not null check (high_value_threshold > 0),
  new_account_days integer not null check (new_account_days > 0),
  account_velocity_limit integer not null check (account_velocity_limit > 0),
  ip_velocity_limit integer not null check (ip_velocity_limit > 0),
  device_velocity_limit integer not null check (device_velocity_limit > 0),
  review_threshold integer not null check (review_threshold between 1 and 100),
  decline_threshold integer not null check (decline_threshold between 1 and 100 and decline_threshold > review_threshold),
  created_at timestamptz not null,
  activated_at timestamptz,
  lock_version bigint not null default 0,
  constraint uk_rule_tenant_version unique (tenant_id,rule_version)
);
create unique index uk_one_active_rule_set_per_tenant on fraud_rule_sets(tenant_id) where status='ACTIVE';
insert into fraud_rule_sets(id,tenant_id,rule_version,status,high_value_threshold,new_account_days,account_velocity_limit,ip_velocity_limit,device_velocity_limit,review_threshold,decline_threshold,created_at,activated_at)
values ('00000000-0000-0000-0000-000000000001','_default',1,'ACTIVE',2000,7,5,10,4,35,70,now(),now());
alter table transaction_decisions add column rule_set_version bigint not null default 1;
