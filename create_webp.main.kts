#!/usr/bin/env kotlin
@file:DependsOn("com.github.ajalt:clikt:2.6.0")


import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import kotlin.math.roundToInt


enum class AndroidImageSize(val factor: Double) {
  MDPI(1.0),
  HDPI(1.5),
  XHDPI(2.0),
  XXHDPI(3.0),
  XXXHDPI(4.0)
}


class CreateWebP : CliktCommand() {

  private val inputFiles: List<File> by argument().file(mustBeReadable = true).multiple()

  private val isAndroid: Boolean by option("-a", "--android").flag()

  private val heightInDp: Int? by option("-dp").int()


  private fun execute(vararg command: String) {
    ProcessBuilder()
      .inheritIO()
      .command(*command)
      .start()
      .waitFor()
      .also { check(it == 0) }
  }

  private val tmpDir = File("/tmp/image_script").apply {
    deleteRecursively()
    mkdirs()
  }

  override fun run() {
    println(inputFiles)
    inputFiles.forEach {
      if (it.extension == "svg") {
        execute("svgo", it.absolutePath)
      }
    }
    if (isAndroid) {
      createAndroidImages()
    } else {
      createSingleLargeImages()
    }
  }

  private fun createSingleLargeImages() {
    inputFiles.forEach { input ->
      val png = input.toPngOrJpg()
      val webp = File(input.parentFile, input.nameWithoutExtension + ".webp")
      execute("cwebp", "-lossless", "-resize", "0", "2048", png.absolutePath, "-o", webp.absolutePath)
    }
  }

  private fun createAndroidImages() {
    val heightInDp = heightInDp ?: throw UsageError("In android-mode you must specify the height in dp too.")
    inputFiles.forEach { input ->
      val losslessWebp = File(tmpDir, "lossless.webp")
      val lossyWebp = File(tmpDir, "lossy.webp")
      val png = input.toPngOrJpg()
      AndroidImageSize.values().forEach {
        lossyWebp.delete()
        losslessWebp.delete()
        val heightInPx = (heightInDp * it.factor).roundToInt()
        execute("cwebp", "-resize", "0", "$heightInPx", "-short", "-lossless", png.absolutePath, "-o", losslessWebp.absolutePath)
        execute("cwebp", "-resize", "0", "$heightInPx", "-short", png.absolutePath, "-o", lossyWebp.absolutePath)
        val pickLossless = losslessWebp.length() < lossyWebp.length()
        val targetWebp = if (pickLossless) losslessWebp else lossyWebp
        val webp = File("drawable-${it.name.toLowerCase()}", input.nameWithoutExtension + ".webp").apply {
          delete()
          parentFile.mkdirs()
        }
        targetWebp.copyTo(webp)
      }
    }
  }

  private fun File.toPngOrJpg(): File {
    return when (extension.toLowerCase()) {
      "svg" -> File(tmpDir, "image.png").also { png ->
        png.delete()
        png.deleteOnExit()
        execute("rsvg-convert", absolutePath, "-h", "4096", "-o", png.absolutePath)
      }
      "jpg", "png", "jpeg" -> this
      else -> throw UsageError("$this is must be a svg or png file")
    }
  }
}

CreateWebP().main(args)
