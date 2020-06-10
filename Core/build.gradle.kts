apply (from = "../jacoco.gradle")

plugins {
    kotlin("multiplatform")

    id("maven-publish"      )
    id("org.jetbrains.dokka")
}

kotlin {
    jvm().compilations.all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    js {
        browser.testTask {
            enabled = false
        }
    }.compilations.all {
        kotlinOptions {
            sourceMap             = true
            moduleKind            = "commonjs"
            sourceMapEmbedSources = "always"
        }
    }

    val mockkVersion   : String by project
    val junitVersion   : String by project
    val log4jVersion   : String by project
    val logbackVersion : String by project
    val mockkJsVersion : String by project
    val measuredVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                api ("com.nectar.measured:measured:$measuredVersion")

                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"      ))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:$mockkVersion")
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation("junit:junit:$junitVersion")
                implementation(kotlin("test-junit"))

                implementation("org.slf4j:slf4j-api:$log4jVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.mockk:mockk-js:$mockkJsVersion")
            }
        }
    }
}