package image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.util.*
import kotlin.math.roundToInt

enum class SizeConstrain {

  Horizontal,

  @Suppress("unused")
  Vertical
}

class CreateWebP : CliktCommand(
  name = "create_webp",
  help = "Converts images to Android Images with their corresponding density buckets"
) {

  private val inputFiles: List<File> by argument().file(mustBeReadable = true).multiple()

  private val dp: Int by option(
    "-dp",
    help = "The size in dp that the resulting webp should have"
  ).int().required()

  private val sizeConstrain: SizeConstrain by option(
    "-c", "--constrain-size",
    help = "If horizontal, the dp size will be applied to the width, if vertical it will be applied to the height"
  )
    .enum<SizeConstrain> { it.name.lowercase() }
    .default(SizeConstrain.Horizontal)

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
    createAndroidImages()
  }

  private fun createAndroidImages() {
    inputFiles.forEach { input ->
      val losslessWebp = File(tmpDir, "lossless.webp")
      val lossyWebp = File(tmpDir, "lossy.webp")
      val png = input.toPngOrJpg()
      val constrainWidth = sizeConstrain == SizeConstrain.Horizontal
      AndroidImageSize.values().forEach {
        lossyWebp.delete()
        losslessWebp.delete()
        val sizeInPx = (dp * it.factor).roundToInt()
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
        val webp = File("drawable-${it.name.lowercase(Locale.ROOT)}", input.nameWithWebpExtension()).apply {
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
    return when (extension.lowercase(Locale.ROOT)) {
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

private enum class AndroidImageSize(val factor: Double) {
  MDPI(1.0),
  HDPI(1.5),
  XHDPI(2.0),
  XXHDPI(3.0),
  XXXHDPI(4.0)
}
