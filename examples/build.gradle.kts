subprojects {
    dependencies {
        implementation(project(":matrix-spring-boot-bot"))
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

        implementation("io.r2dbc:r2dbc-h2")
        implementation("com.h2database:h2")
    }

    tasks.getByName<Jar>("jar") {
        enabled = false
    }

    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        enabled = true
    }
}
