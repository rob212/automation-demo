output "email" {
  description = "The email address of the service account"
  value       = google_service_account.github_actions.email
}

output "key" {
  description = "The base64 encoded service account key"
  value       = google_service_account_key.github_actions_key.private_key
  sensitive   = true
}

output "artifact_registry_repository" {
  description = "The Artifact Registry repository details"
  value = {
    name     = google_artifact_registry_repository.app_repository.name
    location = google_artifact_registry_repository.app_repository.location
  }
}