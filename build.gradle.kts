plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

val libName = "mfm-multiplatform"
val libGroup = "moe.tlaster"
val libVersion = "0.2.0"

group = libGroup
version = libVersion

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()
    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        browser()
        nodejs()
    }
    //    @OptIn(ExperimentalWasmDsl::class)
    //    wasm {
    //        browser()
    //        nodejs()
    //        d8()
    //    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    mingwX64()
    linuxX64()
    linuxArm64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(
        groupId = libGroup,
        artifactId = libName,
        version = libVersion,
    )
    pom {
        name.set(libName)
        description.set("MFM parser")
        url.set("https://github.com/Tlaster/mfm-multiplatform")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("Tlaster")
                name.set("James Tlaster")
                email.set("tlaster@outlook.com")
            }
        }
        scm {
            url.set("https://github.com/Tlaster/mfm-multiplatform")
            connection.set("scm:git:git://github.com/Tlaster/mfm-multiplatform.git")
            developerConnection.set("scm:git:git://github.com/Tlaster/mfm-multiplatform.git")
        }
    }
}

ktlint {
    version.set("1.5.0")
}
