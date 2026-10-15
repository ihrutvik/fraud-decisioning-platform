# AWS deployment

This Terraform stack deploys the application as a private, multi-AZ ECS Fargate service behind an HTTPS Application Load Balancer. It provisions an immutable ECR repository, encrypted RDS PostgreSQL, encrypted ElastiCache Redis, CloudWatch logs, Secrets Manager integration, deployment rollback, and CPU-based horizontal scaling.

The stack accepts an existing VPC, public/private subnets, ACM certificate, and Kafka bootstrap endpoints. Keeping networking and Amazon MSK outside this application stack lets platform teams apply organization-wide routing, inspection, and Kafka authentication standards without coupling their lifecycle to one service.

## Prerequisites

- two or more public subnets for the load balancer;
- two or more private subnets with NAT or VPC endpoints for Fargate tasks;
- an ACM certificate in the deployment region;
- reachable Kafka brokers and an OTLP collector;
- a remote encrypted Terraform backend with state locking for team use.

## Deploy

```bash
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan -out deployment.tfplan
terraform apply deployment.tfplan
```

After the first apply, populate the created application secret with JSON keys `merchant_api_key_sha256` and `analyst_api_key_sha256`, push the image identified by `image_tag` to the ECR output, and force a new ECS deployment. Never commit secret values or `terraform.tfvars`.

## Production controls

- Tasks run without public IPs and only accept port 8080 traffic from the ALB security group.
- RDS and Redis accept traffic only from the task security group.
- RDS uses an AWS-managed master password, Multi-AZ failover, encryption, backups, deletion protection, and Performance Insights.
- Redis uses two encrypted nodes with automatic failover and snapshots.
- ECS deployment circuit breaking automatically rolls back unhealthy task revisions.
- Autoscaling maintains at least two tasks across availability zones and scales on average CPU.
- ECR scanning and immutable tags make deployed artifacts auditable.

For Amazon MSK clusters using IAM authentication, add the AWS MSK IAM auth library and SASL client properties before using port 9098. The application intentionally does not claim IAM support until that client path is implemented and tested.
