
variable "project_id" {
  description = "The GCP project ID where the service account will be created"
  type        = string
}

variable "environment" {
  description = "The environment name (dev, staging, prod)"
  type        = string
}

variable "service_account_id" {
  description = "The ID to use for the service account"
  type        = string
  default     = "github-actions-sa"
}