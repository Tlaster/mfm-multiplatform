plugins {
    kotlin("multiplatform") version "2.3.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.17"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

val libName = "mfm-multiplatform"
val libGroup = "moe.tlaster"
val libVersion = "0.2.8"

group = libGroup
version = libVersion

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
    }
    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }
    jvm {
        compilations.create("benchmark") {
            associateWith(this@jvm.compilations.getByName("main"))
        }
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    wasmJs()
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
        val jvmBenchmark by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.17")
            }
        }
    }
}

benchmark {
    targets {
        register("jvmBenchmark")
    }
    configurations {
        named("main") {
            warmups = 3
            iterations = 5
            iterationTime = 500
            iterationTimeUnit = "ms"
            outputTimeUnit = "ms"
        }
        register("smoke") {
            include("MFMParserBenchmark.parseShort")
            warmups = 1
            iterations = 2
            iterationTime = 200
            iterationTimeUnit = "ms"
            outputTimeUnit = "ms"
        }
        register("stress") {
            include("MFMParserBenchmark.parse(Batch|Large|Function|Unmatched|Deep)")
            warmups = 3
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
            outputTimeUnit = "ms"
        }
        register("optimization") {
            include("MFMParserBenchmark.parse(LargePunctuation|LargeCjk|LongPlainTag|SparseUnicode|DenseUnicode|ComplexUnicode|QuoteHeavy|Unmatched)")
            warmups = 2
            iterations = 5
            iterationTime = 500
            iterationTimeUnit = "ms"
            outputTimeUnit = "ms"
        }
        register("emojiMatcher") {
            include("EmojiMatcherBenchmark")
            warmups = 3
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
            mode = "avgt"
            outputTimeUnit = "ns"
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
