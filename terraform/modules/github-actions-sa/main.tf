# Enable required APIs
resource "google_service_account" "github_actions" {
  project      = var.project_id
  account_id   = var.service_account_id
  display_name = "GitHub Actions Service Account for ${var.environment}"
  description  = "Service account used by GitHub Actions to deploy to ${var.environment}"
}

# First, assign the Service Usage Admin role
resource "google_project_iam_member" "service_usage_admin" {
  project = var.project_id
  role    = "roles/serviceusage.serviceUsageAdmin"
  member  = "serviceAccount:${google_service_account.github_actions.email}"
}

# Then enable required APIs
resource "google_project_service" "required_apis" {
  depends_on = [google_project_iam_member.service_usage_admin]
  
  for_each = toset([
    "artifactregistry.googleapis.com",
    "run.googleapis.com",
    "iam.googleapis.com",
    "cloudresourcemanager.googleapis.com"
  ])
  
  project = var.project_id
  service = each.value
  
  disable_dependent_services = false
  disable_on_destroy        = false
}

# Create Artifact Registry Repository
resource "google_artifact_registry_repository" "app_repository" {
  depends_on = [google_project_service.required_apis]
  
  project       = var.project_id
  location      = var.region
  repository_id = "spring-boot-api"
  description   = "Docker repository for Spring Boot API"
  format        = "DOCKER"
}

# Assign remaining required roles
resource "google_project_iam_member" "service_account_roles" {
  depends_on = [google_project_service.required_apis]
  
  for_each = toset([
    "roles/run.admin",                # Manage Cloud Run services
    "roles/artifactregistry.writer",  # Push to Artifact Registry
    "roles/storage.admin",            # Full access to GCS (for Terraform state)
    "roles/iam.serviceAccountUser",   # Use service accounts
    "roles/iam.serviceAccountAdmin",  # Create and manage service accounts
    "roles/resourcemanager.projectIamAdmin"  # Manage project IAM bindings
  ])
  
  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.github_actions.email}"
}

# Additional environment-specific roles
resource "google_project_iam_member" "environment_specific_roles" {
  depends_on = [google_project_service.required_apis]
  
  for_each = var.environment == "production" ? toset([
    "roles/monitoring.viewer",    # View monitoring in production
    "roles/logging.viewer"        # View logs in production
  ]) : toset([])
  
  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.github_actions.email}"
}

# Create a service account key
resource "google_service_account_key" "github_actions_key" {
  service_account_id = google_service_account.github_actions.name
  public_key_type    = "TYPE_X509_PEM_FILE"
}