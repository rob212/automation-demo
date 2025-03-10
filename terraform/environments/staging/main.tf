module "spring_boot_api" {
  source = "../../modules/spring-boot-api"

  project_id  = var.project_id
  region      = var.region
  service_name = var.service_name
  environment = var.environment
  
  # Staging-specific resource allocations
  cpu    = "1"
  memory = "768Mi"
  
  # Staging-specific environment variables
  env_variables = {
    LOG_LEVEL = "INFO"
    # Add other staging-specific variables here
  }
}

output "service_url" {
  value = module.spring_boot_api.service_url
}