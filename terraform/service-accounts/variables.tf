# terraform/service-accounts/variables.tf

variable "dev_project_id" {
  description = "The GCP project ID for development environment"
  type        = string
}

variable "staging_project_id" {
  description = "The GCP project ID for staging environment"
  type        = string
}

variable "prod_project_id" {
  description = "The GCP project ID for production environment"
  type        = string
}