resource "google_project_service" "run_api" {
  project            = var.project_id
  service            = "run.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "artifactregistry_api" {
  project            = var.project_id
  service            = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

# Create service account for Cloud Run
resource "google_service_account" "cloud_run_service_account" {
  project      = var.project_id
  account_id   = "${var.service_name}-sa"
  display_name = "Service Account for ${var.service_name}"
}

# Grant necessary permissions to service account
resource "google_project_iam_member" "cloud_run_invoker" {
  project = var.project_id
  role    = "roles/run.invoker"
  member  = "serviceAccount:${google_service_account.cloud_run_service_account.email}"
}

# Cloud Run service
resource "google_cloud_run_service" "spring_boot_api" {
  name     = var.service_name
  location = var.region
  project  = var.project_id

  template {
    spec {
      containers {
        image = "${var.region}-docker.pkg.dev/${var.project_id}/spring-boot-api/${var.service_name}:latest"
        
        resources {
          limits = {
            cpu    = var.cpu
            memory = var.memory
          }
        }
        
        env {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        }
        
        # Environment-specific env variables
        dynamic "env" {
          for_each = var.env_variables
          content {
            name  = env.key
            value = env.value
          }
        }
      }
      service_account_name = google_service_account.cloud_run_service_account.email
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }

  depends_on = [
    google_project_service.run_api
  ]

  autogenerate_revision_name = true
}

# Make the Cloud Run service publicly accessible
resource "google_cloud_run_service_iam_member" "public_access" {
  project  = var.project_id
  service  = google_cloud_run_service.spring_boot_api.name
  location = google_cloud_run_service.spring_boot_api.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}