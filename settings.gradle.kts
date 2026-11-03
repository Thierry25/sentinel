pluginManagement {
  plugins {
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "sentinel"

include(
  "sentinel-model", "sentinel-simulator", "sentinel-ingest", "sentinel-rules", "sentinel-sql", "sentinel-alerts", "sentinel-batch",
  "drills-jpa",
)