subprojects {
    dependencies {
        implementation(project(":matrix-spring-boot-bot"))

        implementation("org.hsqldb:hsqldb")
    }

    tasks.getByName<Jar>("jar") {
        enabled = false
    }

    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        enabled = true
    }
}


