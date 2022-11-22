plugins {
  id("org.jetbrains.kotlin.jvm") version "1.5.31"
  application
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("com.github.ajalt.clikt:clikt:3.5.0")
  implementation("com.android.tools:sdk-common:30.3.1")
  implementation("com.android.tools:common:30.3.1")
}

application {
  mainClass.set("image.AppKt")
  applicationName = "android_images"
}
