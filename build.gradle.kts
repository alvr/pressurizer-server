import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.alvr"
version = "0.1.0"

object Versions {
    const val KONF = "0.12"
    const val KOTLIN = "1.3.20"
    const val KTOR = "1.1.1"
    const val LOGBACK = "1.2.3"
}

val codacy: Configuration by configurations.creating

plugins {
    application
    jacoco
    kotlin("jvm") version "1.3.20"
    id("com.github.johnrengelman.shadow") version "4.0.3"
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
        implementation("$k-server-netty:${Versions.KTOR}")
    }

    implementation("com.uchuhimo", "konf", Versions.KONF) {
        exclude("com.fasterxml.jackson.core")
        exclude("com.moandjiezana.toml", "toml4j")
        exclude("org.apiguardian", "apiguardian-api")
        exclude("org.dom4j", "dom4j")
        exclude("org.eclipse.jgit", "org.eclipse.jgit")
        exclude("org.yaml", "snakeyaml")
    }

    implementation("ch.qos.logback", "logback-classic", Versions.LOGBACK)

    codacy("com.github.codacy:codacy-coverage-reporter:-SNAPSHOT")
}

application {
    mainClassName = "me.alvr.pressurizer.PressurizerKt"
}

jacoco {
    toolVersion = "0.8.2"
}

tasks {
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
