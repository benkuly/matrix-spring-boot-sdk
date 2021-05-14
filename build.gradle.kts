import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    base
    id("org.springframework.boot") version Versions.springBoot apply false
    id("io.spring.dependency-management") version Versions.springDependencyManagement apply false
    kotlin("jvm") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
    kotlin("plugin.spring") version Versions.kotlin apply false
    `maven-publish`
    signing
}

allprojects {
    apply(plugin = "kotlin")

    group = "net.folivo"
    version = "0.5.0"
    java.sourceCompatibility = JavaVersion.VERSION_11

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

inline val Project.isRelease
    get() = !version.toString().contains('-')

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
        testImplementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
        testImplementation("io.kotest:kotest-property:${Versions.kotest}")
        testImplementation("io.kotest:kotest-extensions-spring:${Versions.kotest}")
        testImplementation("io.kotest:kotest-extensions-mockserver:${Versions.kotest}")

        testImplementation("com.ninja-squad:springmockk:${Versions.springMockk}")

        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
            exclude(group = "org.mockito", module = "mockito-core")
            exclude(group = "org.mockito", module = "mockito-junit-jupiter")
        }
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    tasks.getByName<Jar>("jar") {
        enabled = true
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<BootJar> {
        enabled = false
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    val projectParent = parent
    if (project.name != "examples" && (projectParent == null || projectParent.name != "examples")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        publishing {
            publications {
                create<MavenPublication>(project.name) {
                    pom {
                        from(components["java"])
                        name.set(project.name)
                        println(project.description)
                        description.set("Spring Boot Starter for [Matrix] bots, clients and appservices based on Trixnity.")
                        url.set("https://gitlab.com/benkuly/matrix-spring-boot-sdk")
                        licenses {
                            license {
                                name.set("GNU Affero General Public License, Version 3.0")
                                url.set("http://www.gnu.org/licenses/agpl-3.0.de.html")
                            }
                        }
                        developers {
                            developer {
                                id.set("benkuly")
                            }
                        }
                        scm {
                            url.set("https://gitlab.com/benkuly/matrix-spring-boot-sdk")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "OSSRH"
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = System.getenv("OSSRH_USERNAME")
                        password = System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
        }

        signing {
            isRequired = isRelease
            useInMemoryPgpKeys(
                System.getenv("OSSRH_PGP_KEY"),
                System.getenv("OSSRH_PGP_PASSWORD")
            )
            sign(publishing.publications)
        }
    }
}