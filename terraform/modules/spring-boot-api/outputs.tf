output "service_url" {
  value = google_cloud_run_service.spring_boot_api.status[0].url
}

output "service_name" {
  value = google_cloud_run_service.spring_boot_api.name
}

output "service_account_email" {
  value = google_service_account.cloud_run_service_account.email
}