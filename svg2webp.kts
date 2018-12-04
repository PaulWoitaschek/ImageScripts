#!/usr/bin/env kscript
@file:DependsOn("org.apache.commons:commons-exec:1.3")
@file:DependsOn("info.picocli:picocli:3.8.1")


import org.apache.commons.exec.DefaultExecutor
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec
import java.io.File
import kotlin.math.roundToInt


enum class AndroidImageSize(val factor: Double) {
  MDPI(1.0),
  HDPI(1.5),
  XHDPI(2.0),
  XXHDPI(3.0),
  XXXHDPI(4.0)
}


@Command(
  name = "Image Converter",
  mixinStandardHelpOptions = true
)
class MyApp : Runnable {

  @Spec
  lateinit var spec: CommandLine.Model.CommandSpec

  @Parameters(description = ["The files to convert"])
  private var inputFiles: List<File> = mutableListOf()

  @Option(names = ["-a", "--android"])
  private var isAndroid: Boolean = false

  @Option(names = ["-dp"])
  private var heightInDp: Int? = null

  private val executor = DefaultExecutor()

  private fun String.execute() {
    executor.execute(org.apache.commons.exec.CommandLine.parse(this))
  }

  private val tmpDir = File("/tmp/image_script").apply {
    deleteRecursively()
    mkdirs()
  }

  override fun run() {
    if (isAndroid) {
      createAndroidImages()
    } else {
      createSingleLargeImages()
    }
  }

  private fun createSingleLargeImages() {
    inputFiles.forEach { input ->
      if (input.extension != "svg") {
        throw ParameterException(spec.commandLine(), "File $input is no svg file")
      }
      val pngTmp = File(tmpDir, "image.png").apply {
        delete()
        deleteOnExit()
      }

      "inkscape ${input.absolutePath} -e ${pngTmp.absolutePath} --without-gui -h 2048x".execute()
      val webp = File(input.parentFile, input.nameWithoutExtension + ".webp")
      "cwebp -lossless ${pngTmp.absolutePath} -o ${webp.absolutePath}".execute()
    }
  }

  private fun createAndroidImages() {
    val heightInDp = heightInDp ?: throw ParameterException(
      spec.commandLine(),
      "In android-mode you  must specify the height in dp too."
    )

    inputFiles.forEach { input ->
      if (input.extension != "svg") {
        throw ParameterException(spec.commandLine(), "File $input is no svg file")
      }
      "svgo ${input.absolutePath}".execute()
      val pngTmp = File(tmpDir, "image.png").apply {
        delete()
        deleteOnExit()
      }
      val losslessWebp = File(tmpDir, "lossless.webp")
      val lossyWebp = File(tmpDir, "lossy.webp")
      AndroidImageSize.values().forEach {
        lossyWebp.delete()
        losslessWebp.delete()
        val heightPx = (heightInDp * it.factor).roundToInt()
        "rsvg-convert ${input.absolutePath} -h $heightPx -o ${pngTmp.absolutePath}".execute()
        "cwebp -short -lossless ${pngTmp.absolutePath} -o ${losslessWebp.absolutePath}".execute()
        "cwebp -short ${pngTmp.absolutePath} -o ${lossyWebp.absolutePath}".execute()
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
}

CommandLine.run(MyApp(), *args)
