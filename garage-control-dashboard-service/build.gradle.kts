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
description = "garage-control-dashboard-service"

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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
                classes(
                    "io.github.lucaspaixaodev.dashboard.GarageControlDashboardServiceApplication",
                    "io.github.lucaspaixaodev.dashboard.GarageControlDashboardServiceApplicationKt",
                    "*Entity",
                    "*EntityRepository",
                    "*Repository",
                    "*Id",
                    "*View",
                    "*Message",
                    "*Config",
                )
                packages(
                    // Server-Sent Events plumbing (emitter lifecycle / framework wiring).
                    "io.github.lucaspaixaodev.dashboard.api.stream",
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
