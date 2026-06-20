plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.3.21"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("org.sonarqube") version "6.2.0.5505"
}

group = "io.github.lucaspaixaodev"
version = "0.0.1-SNAPSHOT"
description = "garage-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-restclient")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.14.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("net.bytebuddy.experimental", "true")
}

kover {
    reports {
        filters {
            excludes {
                // No business logic: framework wiring, persistence/DTO/port holders.
                classes(
                    "io.github.lucaspaixaodev.garageservice.GarageServiceApplication",
                    "io.github.lucaspaixaodev.garageservice.GarageServiceApplicationKt",
                    "*Entity",
                    "*EntityRepository",
                    "*Response",
                    "*Request",
                    "*Result",
                    "*GarageResponse*",
                )
                packages(
                    // Output gateway port + its data carriers (interfaces / DTOs).
                    "io.github.lucaspaixaodev.garageservice.application.garage.gateway",
                    // Repository ports (interfaces).
                    "io.github.lucaspaixaodev.garageservice.application.garage.repository",
                    "io.github.lucaspaixaodev.garageservice.application.spot.repository",
                )
            }
        }
        verify {
            rule {
                minBound(90)
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "garage-service")
        property("sonar.projectName", "garage-service")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "http://localhost:9000")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/report.xml").get().asFile.path,
        )
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/GarageServiceApplication.kt",
                "**/*Entity.kt",
                "**/*EntityRepository.kt",
                "**/*Response*.kt",
                "**/*Request.kt",
                "**/application/garage/gateway/**",
                "**/application/*/repository/*Repository.kt",
            ).joinToString(","),
        )
    }
}

tasks.named("sonar") {
    dependsOn("koverXmlReport")
}
