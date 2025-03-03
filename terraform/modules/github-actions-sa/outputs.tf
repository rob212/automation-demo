
output "email" {
  description = "The email address of the service account"
  value       = google_service_account.github_actions.email
}

output "key" {
  description = "The service account key (base64-encoded JSON)"
  value       = google_service_account_key.github_actions_key.private_key
  sensitive   = true
}