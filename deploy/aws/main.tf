locals {
  resource_name = "${var.name}-${var.environment}"
}

data "aws_caller_identity" "current" {}

resource "aws_ecr_repository" "app" {
  name                 = local.resource_name
  image_tag_mutability = "IMMUTABLE"
  image_scanning_configuration { scan_on_push = true }
  encryption_configuration { encryption_type = "AES256" }
}

resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name
  policy = jsonencode({ rules = [{
    rulePriority = 1
    description  = "Retain the latest 30 images"
    selection    = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 30 }
    action       = { type = "expire" }
  }] })
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.resource_name}"
  retention_in_days = 30
}

resource "aws_secretsmanager_secret" "application" {
  name                    = "${local.resource_name}/application"
  recovery_window_in_days = 7
  description             = "JSON keys: merchant_api_key_sha256 and analyst_api_key_sha256"
}

resource "aws_security_group" "alb" {
  name        = "${local.resource_name}-alb"
  description = "Public HTTPS ingress"
  vpc_id      = var.vpc_id
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "app" {
  name        = "${local.resource_name}-app"
  description = "Only the ALB can reach application tasks"
  vpc_id      = var.vpc_id
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "data" {
  name        = "${local.resource_name}-data"
  description = "PostgreSQL and Redis ingress from application tasks"
  vpc_id      = var.vpc_id
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
}

resource "aws_db_subnet_group" "this" {
  name       = local.resource_name
  subnet_ids = var.private_subnet_ids
}

resource "aws_db_instance" "this" {
  identifier                     = local.resource_name
  engine                         = "postgres"
  engine_version                 = "16"
  instance_class                 = var.database_instance_class
  allocated_storage              = var.database_allocated_storage
  max_allocated_storage          = var.database_allocated_storage * 4
  storage_type                   = "gp3"
  storage_encrypted              = true
  db_name                        = "fraud"
  username                       = "fraudadmin"
  manage_master_user_password    = true
  db_subnet_group_name           = aws_db_subnet_group.this.name
  vpc_security_group_ids         = [aws_security_group.data.id]
  multi_az                       = true
  publicly_accessible            = false
  backup_retention_period        = 14
  deletion_protection            = true
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  auto_minor_version_upgrade     = true
  performance_insights_enabled   = true
  skip_final_snapshot            = false
  final_snapshot_identifier      = "${local.resource_name}-final"
}

resource "aws_elasticache_subnet_group" "this" {
  name       = local.resource_name
  subnet_ids = var.private_subnet_ids
}

resource "aws_elasticache_replication_group" "this" {
  replication_group_id       = local.resource_name
  description                = "Fraud velocity windows"
  engine                     = "redis"
  node_type                  = var.redis_node_type
  port                       = 6379
  num_cache_clusters         = 2
  automatic_failover_enabled = true
  multi_az_enabled           = true
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  subnet_group_name          = aws_elasticache_subnet_group.this.name
  security_group_ids         = [aws_security_group.data.id]
  snapshot_retention_limit   = 7
  apply_immediately          = false
}

resource "aws_lb" "this" {
  name               = substr(local.resource_name, 0, 32)
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.public_subnet_ids
  enable_deletion_protection = true
}

resource "aws_lb_target_group" "app" {
  name        = substr("${local.resource_name}-app", 0, 32)
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.vpc_id
  health_check {
    path                = "/actuator/health/readiness"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 15
    timeout             = 5
    matcher             = "200"
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.this.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = var.certificate_arn
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

resource "aws_ecs_cluster" "this" {
  name = local.resource_name
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_iam_role" "execution" {
  name = "${local.resource_name}-execution"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{
    Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" }, Action = "sts:AssumeRole"
  }] })
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "secrets" {
  name = "read-runtime-secrets"
  role = aws_iam_role.execution.id
  policy = jsonencode({ Version = "2012-10-17", Statement = [{
    Effect = "Allow", Action = ["secretsmanager:GetSecretValue"],
    Resource = [aws_secretsmanager_secret.application.arn, aws_db_instance.this.master_user_secret[0].secret_arn]
  }] })
}

resource "aws_iam_role" "task" {
  name = "${local.resource_name}-task"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{
    Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" }, Action = "sts:AssumeRole"
  }] })
}

resource "aws_ecs_task_definition" "app" {
  family                   = local.resource_name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  container_definitions = jsonencode([{
    name      = "app"
    image     = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
    essential = true
    portMappings = [{ containerPort = 8080, protocol = "tcp" }]
    environment = [
      { name = "DATABASE_URL", value = "jdbc:postgresql://${aws_db_instance.this.address}:5432/fraud?sslmode=require" },
      { name = "REDIS_HOST", value = aws_elasticache_replication_group.this.primary_endpoint_address },
      { name = "REDIS_PORT", value = "6379" },
      { name = "REDIS_SSL_ENABLED", value = "true" },
      { name = "KAFKA_BOOTSTRAP_SERVERS", value = var.kafka_bootstrap_servers },
      { name = "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT", value = var.otel_traces_endpoint },
      { name = "TRACING_SAMPLING_PROBABILITY", value = "0.1" },
      { name = "VELOCITY_CIRCUIT_FAILURE_THRESHOLD", value = "3" },
      { name = "VELOCITY_CIRCUIT_OPEN_SECONDS", value = "30" }
    ]
    secrets = [
      { name = "DATABASE_USERNAME", valueFrom = "${aws_db_instance.this.master_user_secret[0].secret_arn}:username::" },
      { name = "DATABASE_PASSWORD", valueFrom = "${aws_db_instance.this.master_user_secret[0].secret_arn}:password::" },
      { name = "MERCHANT_API_KEY_SHA256", valueFrom = "${aws_secretsmanager_secret.application.arn}:merchant_api_key_sha256::" },
      { name = "ANALYST_API_KEY_SHA256", valueFrom = "${aws_secretsmanager_secret.application.arn}:analyst_api_key_sha256::" }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = { awslogs-group = aws_cloudwatch_log_group.app.name, awslogs-region = var.aws_region, awslogs-stream-prefix = "app" }
    }
    healthCheck = { command = ["CMD-SHELL", "wget -q -O /dev/null http://localhost:8080/actuator/health/liveness || exit 1"], interval = 30, timeout = 5, retries = 3, startPeriod = 60 }
  }])
}

resource "aws_ecs_service" "app" {
  name            = local.resource_name
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"
  platform_version = "LATEST"
  enable_execute_command = true
  health_check_grace_period_seconds = 90
  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = false
  }
  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = 8080
  }
  depends_on = [aws_lb_listener.https]
}

resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  name               = "${local.resource_name}-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace
  target_tracking_scaling_policy_configuration {
    predefined_metric_specification { predefined_metric_type = "ECSServiceAverageCPUUtilization" }
    target_value       = 60
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
