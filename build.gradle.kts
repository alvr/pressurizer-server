import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.alvr"
version = "0.5.0"

object Versions {
    const val CFG4K = "0.9.3"
    const val KOTLIN = "1.3.30"
    const val EXPOSED = "0.13.6"
    const val HIKARI = "3.3.1"
    const val JSOUP = "1.11.3"
    const val KOTLINTEST = "3.3.1"
    const val KTOR = "1.1.3"
    const val LOGBACK = "1.2.3"
    const val MOCKSERVER = "5.5.1"
    const val POSTGRES = "42.2.5"
}

val codacy: Configuration by configurations.creating

plugins {
    application
    jacoco
    kotlin("jvm") version "1.3.21"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.adarshr.test-logger") version "1.6.0"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", Versions.KOTLIN))

    "io.ktor:ktor".also { k ->
        implementation("$k-auth-jwt:${Versions.KTOR}")
        implementation("$k-client-apache:${Versions.KTOR}")
        implementation("$k-client-core:${Versions.KTOR}")
        implementation("$k-client-gson:${Versions.KTOR}") {
            exclude("org.jetbrains.kotlinx")
        }
        implementation("$k-gson:${Versions.KTOR}")
        implementation("$k-locations:${Versions.KTOR}")
        implementation("$k-server-netty:${Versions.KTOR}")
        testImplementation("$k-server-test-host:${Versions.KTOR}")
    }

    implementation("com.jdiazcano.cfg4k", "cfg4k-core", Versions.CFG4K)
    implementation("com.jdiazcano.cfg4k", "cfg4k-hocon", Versions.CFG4K)
    
    implementation("org.jetbrains.exposed", "exposed", Versions.EXPOSED)
    implementation("org.postgresql", "postgresql", Versions.POSTGRES)
    implementation("com.zaxxer", "HikariCP", Versions.HIKARI)

    implementation("org.jsoup", "jsoup", Versions.JSOUP)

    implementation("ch.qos.logback", "logback-classic", Versions.LOGBACK)

    testImplementation("io.kotlintest", "kotlintest-runner-junit5", Versions.KOTLINTEST)
    testImplementation("org.mock-server", "mockserver-netty", Versions.MOCKSERVER)

    codacy("com.github.codacy:codacy-coverage-reporter:-SNAPSHOT")
}

application {
    mainClassName = "me.alvr.pressurizer.PressurizerKt"
}

jacoco {
    toolVersion = "0.8.3"
}

tasks {
    withType<JavaExec> {
        environment("OPENID","https://steamcommunity.com/openid/login")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<ShadowJar> {
        archiveBaseName.set("pressurizer")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    withType<Test> {
        useJUnitPlatform()
        environment("OPENID","http://localhost:6969")

        testlogger {
            theme = ThemeType.MOCHA
        }

        val jacocoReport = withType<JacocoReport> {
            reports {
                xml.isEnabled = true
            }
        }

        finalizedBy(jacocoReport)
    }

    register<JavaExec>("uploadReportCodacy") {
        main = "com.codacy.CodacyCoverageReporter"
        classpath = codacy

        args = listOf(
            "report",
            "-l",
            "Kotlin",
            "-r",
            "$buildDir/reports/jacoco/test/jacocoTestReport.xml",
            "--prefix",
            "src/main/kotlin/"
        )
    }
}
