#!/usr/bin/env kscript
@file:DependsOn("com.beust:jcommander:1.71")
@file:DependsOn("org.apache.commons:commons-exec:1.3")
@file:DependsOn("com.android.tools:sdk-common:26.5.0")
@file:MavenRepository("google", "https://dl.google.com/dl/android/maven2")


import com.android.ide.common.vectordrawable.Svg2Vector
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
                executor.execute(CommandLine.parse("svgo \"${input.absolutePath}\" -p 1"))
                val baseNameCleaned = input.nameWithoutExtension
                        .replace('.', '_')
                        .replace('-', '_')
                val xml = File(input.parentFile, "$baseNameCleaned.xml")

                xml.outputStream().use {
                    val error = Svg2Vector.parseSvgToXml(input, it)
                    if (error.isNotEmpty()) {
                        println("Error in Svg2Vector for $input\n$error")
                    } else {
                        executor.execute(CommandLine.parse("avocado '${xml.absolutePath}'"))
                    }
                }
            }
}
