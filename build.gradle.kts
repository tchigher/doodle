buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven       { url = uri("https://plugins.gradle.org/m2/"          ) }
        mavenCentral()
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    val kotlinVersion: String by System.getProperties()

    id ("org.jetbrains.kotlin.multiplatform") version kotlinVersion
    id ("org.jetbrains.dokka"               ) version "0.10.0"
}

allprojects {
    version = "0.1.0"
    group   = "io.nacular.doodle"

    repositories {
        maven       { url = uri("http://dl.bintray.com/kotlin/kotlin-eap") }
        mavenCentral()
        mavenLocal  ()
        jcenter     ()
    }

    apply(plugin = "org.jetbrains.dokka")

    tasks {
        val dokka by getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
            outputFormat = "html"

            outputDirectory = "$buildDir/dokka"
            subProjects = listOf("Animation", "Browser", "Controls", "Core", "Themes")

            configuration {
                // Use to include or exclude non public members.
                includeNonPublic = false

                // Do not output deprecated members. Applies globally, can be overridden by packageOptions
                skipDeprecated = true

                // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
                reportUndocumented = true

                // Do not create index pages for empty packages
                skipEmptyPackages = true
            }
        }
    }
}