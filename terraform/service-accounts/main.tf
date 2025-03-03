
provider "google" {
  # No default project specified here, as we'll define different projects for each environment
}

# Service account for development environment
module "github_actions_dev" {
  source = "../modules/github-actions-sa"
  
  project_id        = var.dev_project_id
  environment       = "development"
  service_account_id = "github-actions-sa"
}

# Service account for staging environment
module "github_actions_staging" {
  source = "../modules/github-actions-sa"
  
  project_id        = var.staging_project_id
  environment       = "staging"
  service_account_id = "github-actions-sa"
}

# Service account for production environment
module "github_actions_prod" {
  source = "../modules/github-actions-sa"
  
  project_id        = var.prod_project_id
  environment       = "production"
  service_account_id = "github-actions-sa"
}

# Output the keys (these will be sensitive)
output "dev_service_account_key" {
  value     = module.github_actions_dev.key
  sensitive = true
}

output "staging_service_account_key" {
  value     = module.github_actions_staging.key
  sensitive = true
}

output "prod_service_account_key" {
  value     = module.github_actions_prod.key
  sensitive = true
}