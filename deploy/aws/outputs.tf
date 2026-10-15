output "ecr_repository_url" { value = aws_ecr_repository.app.repository_url }
output "load_balancer_dns_name" { value = aws_lb.this.dns_name }
output "ecs_cluster_name" { value = aws_ecs_cluster.this.name }
output "ecs_service_name" { value = aws_ecs_service.app.name }
output "application_secret_arn" { value = aws_secretsmanager_secret.application.arn }
output "database_secret_arn" {
  value     = aws_db_instance.this.master_user_secret[0].secret_arn
  sensitive = true
}
