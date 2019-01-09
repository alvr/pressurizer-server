import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.alvr"
version = "0.1.0"

object Versions {
    const val KOTLIN = "1.3.11"
    const val KTOR = "1.1.1"
    const val LOGBACK = "1.2.3"
}

plugins {
    application
    kotlin("jvm") version "1.3.11"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", Versions.KOTLIN))
    implementation("io.ktor", "ktor-server-netty", Versions.KTOR)

    implementation("ch.qos.logback", "logback-classic", Versions.LOGBACK)
}

application {
    mainClassName = "me.alvr.pressurizer.PressurizerKt"
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
}