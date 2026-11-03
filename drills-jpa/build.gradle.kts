plugins {
    id("sentinel.java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.ibm.db2:jcc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    onlyIf("needs the local DB2") {
        project.hasProperty("withDb2")
    }
}

tasks.bootJar {
    enabled = false
}