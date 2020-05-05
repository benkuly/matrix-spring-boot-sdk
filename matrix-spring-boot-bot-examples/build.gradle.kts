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

tasks {
    register<Exec>("createITInfra") {
        workingDir = File("src/main/podman/")
        group = "infrastructure"
        commandLine("podman", "play", "kube", "localinfra.yaml")
    }
    register<Exec>("restartITInfra") {
        workingDir = File("src/main/podman/")
        group = "infrastructure"
        doFirst {
            exec {
                isIgnoreExitValue = true
                commandLine("podman", "pod", "stop", "matrix-spring-boot-bot-examples")
            }
            exec {
                isIgnoreExitValue = true
                commandLine("podman", "pod", "rm", "matrix-spring-boot-bot-examples", "-f")
            }
        }
        commandLine("podman", "play", "kube", "localinfra.yaml")
    }
    register<Exec>("startITInfra") {
        group = "infrastructure"
        commandLine("podman", "pod", "start", "matrix-spring-boot-bot-examples")
    }
    register<Exec>("stopITInfra") {
        group = "infrastructure"
        commandLine("podman", "pod", "stop", "matrix-spring-boot-bot-examples")
    }
}
