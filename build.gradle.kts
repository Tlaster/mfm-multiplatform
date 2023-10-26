import com.vanniktech.maven.publish.SonatypeHost

plugins {
  kotlin("multiplatform") version "1.9.0"
  id("org.jetbrains.kotlinx.kover") version "0.7.3"
  id("com.vanniktech.maven.publish") version "0.25.3"
  id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

group = "moe.tlaster"
version = "0.1.1"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    jvmToolchain(11)
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
  ios()
  iosSimulatorArm64()
  macosX64()
  macosArm64()
  watchos()
  watchosSimulatorArm64()
  tvos()
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
  publishToMavenCentral(SonatypeHost.S01)
  signAllPublications()
  coordinates(
    groupId = "moe.tlaster",
    artifactId = "mfm-multiplatform",
    version = "0.1.1-SNAPSHOT",
  )
  pom {
    name.set("mfm-multiplatform")
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
  version.set("0.50.0")
}
