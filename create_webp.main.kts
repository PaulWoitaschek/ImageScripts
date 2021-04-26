#!/usr/bin/env kotlin
@file:DependsOn("com.github.ajalt:clikt:2.7.1")

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

  private val dp: Int? by option("-dp").int()
  private val constrainWidth by option("-cw", "--constrain-width").flag(default = true)

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
      val webp = File(input.parentFile, input.nameWithWebpExtension())
      execute(
        "cwebp",
        "-lossless",
        "-resize",
        if (constrainWidth) "2048" else "0",
        if (constrainWidth) "0" else "2048",
        png.absolutePath,
        "-o",
        webp.absolutePath
      )
    }
  }

  private fun createAndroidImages() {
    val size = dp ?: throw UsageError("In android-mode you must specify the size in dp too.")
    inputFiles.forEach { input ->
      val losslessWebp = File(tmpDir, "lossless.webp")
      val lossyWebp = File(tmpDir, "lossy.webp")
      val png = input.toPngOrJpg()
      AndroidImageSize.values().forEach {
        lossyWebp.delete()
        losslessWebp.delete()
        val sizeInPx = (size * it.factor).roundToInt()
        execute(
          "cwebp",
          "-resize",
          if (constrainWidth) "$sizeInPx" else "0",
          if (constrainWidth) "0" else "$sizeInPx",
          "-short",
          "-lossless",
          png.absolutePath,
          "-o",
          losslessWebp.absolutePath
        )
        execute(
          "cwebp",
          "-resize",
          if (constrainWidth) "$sizeInPx" else "0",
          if (constrainWidth) "0" else "$sizeInPx",
          "-short",
          png.absolutePath,
          "-o",
          lossyWebp.absolutePath
        )
        val pickLossless = losslessWebp.length() < lossyWebp.length()
        val targetWebp = if (pickLossless) losslessWebp else lossyWebp
        val webp = File("drawable-${it.name.toLowerCase()}", input.nameWithWebpExtension()).apply {
          delete()
          parentFile.mkdirs()
        }
        targetWebp.copyTo(webp)
      }
    }
  }

  private fun File.nameWithWebpExtension(): String {
    return nameWithoutExtension
      .run {
        if (get(0).isDigit()) {
          "image_$this"
        } else {
          this
        }
      }
      .replace(".", "_")
      .replace("-", "_")
      .plus(".webp")
  }

  private fun File.toPngOrJpg(): File {
    return when (extension.toLowerCase()) {
      "svg" -> File(tmpDir, "image.png").also { png ->
        png.delete()
        png.deleteOnExit()
        execute("rsvg-convert", absolutePath, "-w", "4096", "-o", png.absolutePath)
      }
      "jpg", "png", "jpeg" -> this
      else -> throw UsageError("$this is must be a svg or png file")
    }
  }
}

CreateWebP().main(args)
