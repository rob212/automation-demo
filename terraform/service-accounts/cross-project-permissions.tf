# Grant cross-project permissions to the Terraform state bucket

# Reference to the bucket in the dev project
data "google_storage_bucket" "terraform_state" {
  name    = "tf-automation-demo-state-spring-boot-api"
  project = var.dev_project_id
}

# Grant the staging service account access to the bucket
resource "google_storage_bucket_iam_member" "staging_bucket_access" {
  bucket = data.google_storage_bucket.terraform_state.name
  role   = "roles/storage.admin"
  member = "serviceAccount:github-actions-sa@${var.staging_project_id}.iam.gserviceaccount.com"
}

# Grant the production service account access to the bucket
resource "google_storage_bucket_iam_member" "prod_bucket_access" {
  bucket = data.google_storage_bucket.terraform_state.name
  role   = "roles/storage.admin"
  member = "serviceAccount:github-actions-sa@${var.prod_project_id}.iam.gserviceaccount.com"
}