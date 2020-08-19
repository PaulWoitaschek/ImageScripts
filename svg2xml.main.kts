#!/usr/bin/env kotlin
@file:Repository("https://maven.google.com")
@file:DependsOn("com.github.ajalt:clikt:2.7.1")
@file:DependsOn("com.android.tools:sdk-common:27.0.1")

import com.android.ide.common.vectordrawable.Svg2Vector
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class SvgToXml : CliktCommand() {

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
    val xml = File(input.parentFile, "$baseNameCleaned.xml")

    xml.outputStream().use {
      try {
        val error = Svg2Vector.parseSvgToXml(input, it)
        if (error.isNotEmpty()) {
          echo("Error in Svg2Vector for $input\n$error")
        } else {
          execute("avocado", xml.absolutePath)
        }
      } catch (e: Exception) {
        e.printStackTrace()
        xml.delete()
      }
    }
  }

  private fun execute(vararg command: String) {
    ProcessBuilder()
      .inheritIO()
      .command(*command)
      .start()
      .waitFor()
      .also { check(it == 0) }
  }
}

SvgToXml().main(args)
