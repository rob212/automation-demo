variable "project_id" {
  description = "The GCP project ID where the service account will be created"
  type        = string
}

variable "environment" {
  description = "The environment (development, staging, production)"
  type        = string
  
  validation {
    condition     = contains(["development", "staging", "production"], var.environment)
    error_message = "Environment must be one of: development, staging, production"
  }
}

variable "service_account_id" {
  description = "The ID to use for the service account"
  type        = string
  default     = "github-actions-sa"
}

variable "region" {
  description = "The region where resources will be created"
  type        = string
  default     = "us-central1"
}