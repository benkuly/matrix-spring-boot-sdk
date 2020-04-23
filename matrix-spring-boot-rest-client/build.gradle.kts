plugins {
    `maven-publish`
    signing
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver")
}

signing {
    useInMemoryPgpKeys(
            System.getenv("MAVEN_SIGN_KEY"),
            System.getenv("MAVEN_SIGN_PASSWORD")
    )
    sign(publishing.publications["mavenJava"])
}

publishing {
    publications {
        create<MavenPublication>("matrix-spring-boot-rest-client") {
            pom {
                artifactId = "matrix-spring-boot-rest-client"

                from(components["java"])

                name.set("matrix-spring-boot-rest-client")
                description.set("Spring Boot Starter for matrix-protocol client.")
                url.set("https://github.com/benkuly/matrix-spring-boot-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("benkuly")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/benkuly/matrix-spring-boot-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/benkuly/matrix-spring-boot-sdk.git")
                    url.set("https://github.com/benkuly/matrix-spring-boot-sdk")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
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