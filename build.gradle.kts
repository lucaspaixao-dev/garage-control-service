plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    kotlin("plugin.jpa") version "2.3.21" apply false
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
    id("org.sonarqube") version "6.2.0.5505"
}

sonar {
    properties {
        property("sonar.projectKey", "garage-service")
        property("sonar.projectName", "garage-service")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "http://localhost:9000")
        // Kover emits a JaCoCo-compatible XML per module; SonarQube merges both.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                "garage-control-service/build/reports/kover/report.xml",
                "garage-control-webhook-service/build/reports/kover/report.xml",
                "garage-control-dashboard-service/build/reports/kover/report.xml",
            ).joinToString(","),
        )
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/*Application.kt",
                "**/*Entity.kt",
                "**/*EntityRepository.kt",
                "**/*Item.kt",
                "**/*Response*.kt",
                "**/*Request.kt",
                "**/*Message.kt",
                "**/*Type.kt",
                "**/config/**",
                "**/application/garage/gateway/**",
                "**/application/*/repository/*Repository.kt",
            ).joinToString(","),
        )
    }
}

tasks.named("sonar") {
    dependsOn(
        ":garage-control-service:koverXmlReport",
        ":garage-control-webhook-service:koverXmlReport",
        ":garage-control-dashboard-service:koverXmlReport",
    )
}
