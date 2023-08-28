plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}

dependencies {
  enforcedPlatform(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation(libs.clikt)
  implementation(libs.androidTools.common)
  implementation(libs.androidTools.sdkCommon)
}

application {
  mainClass.set("image.AppKt")
  applicationName = "android_images"
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}

kotlin {
  jvmToolchain(20)
}
