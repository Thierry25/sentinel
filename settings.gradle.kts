plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "sentinel"


include(
  "sentinel-model", "sentinel-simulator", "sentinel-ingest", "sentinel-rules", "sentinel-sql", "sentinel-alerts", "sentinel-batch",
)