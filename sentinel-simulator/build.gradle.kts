plugins {
    id("sentinel.java-conventions")
    application
}

dependencies {
    implementation(project(":sentinel-model"))
}

application {
    mainClass = "com.sentinel.simulator.SimulatorApp"
}