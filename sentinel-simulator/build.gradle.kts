plugins {
    id("sentinel.java-conventions")
    application
}

dependencies {
    implementation(project(":sentinel-model"))
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    testImplementation("org.testcontainers:kafka:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

application {
    mainClass = "com.sentinel.simulator.SimulatorApp"
}