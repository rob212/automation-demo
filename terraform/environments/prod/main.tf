module "spring_boot_api" {
  source = "../../modules/spring-boot-api"

  project_id  = var.project_id
  region      = var.region
  service_name = var.service_name
  environment = var.environment
  
  # Production-specific resource allocations
  cpu    = "2000m"
  memory = "1Gi"
  
  # Production-specific environment variables
  env_variables = {
    LOG_LEVEL = "WARN"
    # Add other production-specific variables here
  }
}

# You might add additional production-specific resources here
# For example, monitoring, alerts, or additional security measures

output "service_url" {
  value = module.spring_boot_api.service_url
}