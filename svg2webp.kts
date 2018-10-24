#!/usr/bin/env kscript
@file:DependsOn("org.apache.commons:commons-exec:1.3", "com.beust:jcommander:1.71")

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import java.io.File

data class Args(
  @Parameter(description = "Files")
  var files: List<String> = ArrayList(),
  @Parameter(names = ["--help"], help = true)
  var help: Boolean = false
)

val arguments = Args()
val jCommander = JCommander.newBuilder().addObject(arguments).build()!!
  .also {
    it.parse(*args)
  }
run(arguments)

fun run(arguments: Args) {
  if (arguments.help) {
    jCommander.usage()
    return
  }

  val executor = DefaultExecutor()
  arguments.files
    .map(::File)
    .toSet()
    .forEach { input ->
      require(input.exists() && input.extension == "svg") {
        "File $input must exist and be a svg file."
      }
      val png = File(input.parentFile, input.nameWithoutExtension + ".png").apply {
        deleteOnExit()
      }
      executor.execute(CommandLine.parse("inkscape ${input.absolutePath} -e ${png.absolutePath} --without-gui -h 2048x"))

      val webp = File(input.parentFile, input.nameWithoutExtension + ".webp")
      executor.execute(CommandLine.parse("cwebp -lossless ${png.absolutePath} -o ${webp.absolutePath}"))
    }
}
