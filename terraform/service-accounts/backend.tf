terraform {
  backend "gcs" {
    bucket = "tf-automation-demo-state-spring-boot-api"
    prefix = "terraform/service-accounts"
  }
}