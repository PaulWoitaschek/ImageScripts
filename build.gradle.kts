plugins {
  id("org.jetbrains.kotlin.jvm") version "1.5.30"
  application
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("com.github.ajalt.clikt:clikt:3.1.0")
  implementation("com.android.tools:sdk-common:30.0.2")
  implementation("com.android.tools:common:30.0.2")
}

application {
  mainClass.set("image.AppKt")
}
