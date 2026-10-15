variable "name" {
  type    = string
  default = "fraud-decisioning-platform"
}
variable "environment" {
  type    = string
  default = "production"
}
variable "aws_region" {
  type    = string
  default = "eu-central-1"
}
variable "vpc_id" { type = string }
variable "public_subnet_ids" { type = list(string) }
variable "private_subnet_ids" { type = list(string) }
variable "certificate_arn" { type = string }
variable "kafka_bootstrap_servers" {
  type      = string
  sensitive = true
}
variable "otel_traces_endpoint" {
  type    = string
  default = ""
}
variable "image_tag" {
  type    = string
  default = "latest"
}
variable "desired_count" {
  type    = number
  default = 2
}
variable "min_capacity" {
  type    = number
  default = 2
}
variable "max_capacity" {
  type    = number
  default = 10
}
variable "task_cpu" {
  type    = number
  default = 1024
}
variable "task_memory" {
  type    = number
  default = 2048
}
variable "database_instance_class" {
  type    = string
  default = "db.t4g.medium"
}
variable "database_allocated_storage" {
  type    = number
  default = 50
}
variable "redis_node_type" {
  type    = string
  default = "cache.t4g.small"
}
