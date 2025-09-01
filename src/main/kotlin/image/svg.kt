package image

import com.android.ide.common.vectordrawable.Svg2Vector
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class SvgToXml : CliktCommand(
  name = "svg2xml",
  help = "Converts svg files to android xml vector drawables."
) {

  private val files by argument().file(mustBeReadable = true).multiple()

  override fun run() {
    files.toSet()
      .forEach { input ->
        convert(input)
      }
  }

  private fun convert(input: File) {
    require(input.exists() && input.extension == "svg") {
      "File $input must exist and be a svg file."
    }
    execute("svgo", input.absolutePath, "-p", "1")
    val baseNameCleaned = input.nameWithoutExtension
      .replace('.', '_')
      .replace('-', '_')
      .replace(' ', '_')
      .lowercase()

    val xml = File(input.parentFile, "$baseNameCleaned.xml")

    xml.outputStream().use {
      try {
        val error = Svg2Vector.parseSvgToXml(input.toPath(), it)
        if (error.isNotEmpty()) {
          echo("Error in Svg2Vector for $input\n$error")
        }
      } catch (e: Exception) {
        e.printStackTrace()
        xml.delete()
      }
      Unit
    }
  }
}
