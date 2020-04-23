dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver")
}

// TODO integration tests
//tasks {
//    register<Exec>("createITInfra") {
//        workingDir = File("src/test/podman/")
//        group = "infrastructure"
//        commandLine("podman", "play", "kube", "matrix-kotlin-sdk-it.yaml")
//    }
//    register<Exec>("restartITInfra") {
//        workingDir = File("src/test/podman/")
//        group = "infrastructure"
//        doFirst {
//            exec {
//                isIgnoreExitValue = true
//                commandLine("podman", "pod", "stop", "matrix-kotlin-sdk-it")
//            }
//            exec {
//                isIgnoreExitValue = true
//                commandLine("podman", "pod", "rm", "matrix-kotlin-sdk-it", "-f")
//            }
//        }
//        commandLine("podman", "play", "kube", "matrix-kotlin-sdk-it.yaml")
//    }
//    register<Exec>("startITInfra") {
//        group = "infrastructure"
//        commandLine("podman", "pod", "start", "matrix-kotlin-sdk-it")
//    }
//    register<Exec>("stopITInfra") {
//        group = "infrastructure"
//        commandLine("podman", "pod", "stop", "matrix-kotlin-sdk-it")
//    }
//}