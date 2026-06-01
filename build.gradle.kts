import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.5.4"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["vaadinVersion"] = "24.5.4"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Vaadin
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // KaribuDSL – idiomatic Kotlin DSL for Vaadin
    implementation("com.github.mvysny.karibudsl:karibu-dsl:2.2.0")

    // REST + OpenAPI / Swagger UI
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // OpenAPI spec generation only (the bundled Swagger UI webjar conflicts with
    // Vaadin's root servlet mapping, so we self-host the UI under /swagger/ instead).
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v24:2.2.0")
    testRuntimeOnly("com.h2database:h2")
}

// Vaadin @Route views need a proxyable (non-final) class because @RolesAllowed
// method security wraps them in a CGLIB subclass. plugin.spring's all-open only
// covers Spring stereotypes, so open @Route classes explicitly.
allOpen {
    annotation("com.vaadin.flow.router.Route")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

vaadin {
    productionMode = System.getenv("VAADIN_PRODUCTION_MODE")?.toBoolean() ?: false
}
