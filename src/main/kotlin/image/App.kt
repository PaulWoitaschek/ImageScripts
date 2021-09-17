package image

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(vararg args: String) {
  App()
    .subcommands(CreateWebP(), SvgToXml())
    .main(args)
}

class App : CliktCommand() {

  override fun run() {}
}
