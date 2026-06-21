plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlinx.kover")
}

group = "io.github.lucaspaixaodev"
version = "0.0.1-SNAPSHOT"
description = "garage-control-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:4.0.2")
    }
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
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
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
                    "*Command",
                    "*Message",
                    "*GarageResponse*",
                )
                packages(
                    "io.github.lucaspaixaodev.garageservice.application.garage.gateway",
                    "io.github.lucaspaixaodev.garageservice.application.garage.repository",
                    "io.github.lucaspaixaodev.garageservice.application.spot.repository",
                    "io.github.lucaspaixaodev.garageservice.application.ticket.repository",
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
