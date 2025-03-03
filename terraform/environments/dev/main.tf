module "spring_boot_api" {
  source = "../../modules/spring-boot-api"

  project_id  = var.project_id
  region      = var.region
  service_name = var.service_name
  environment = var.environment
  
  # Dev-specific resource allocations
  cpu    = "1000m"
  memory = "512Mi"
  
  # Dev-specific environment variables
  env_variables = {
    LOG_LEVEL = "DEBUG"
    # Add other dev-specific variables here
  }
}

output "service_url" {
  value = module.spring_boot_api.service_url
}