package image

internal fun execute(vararg command: String) {
  ProcessBuilder()
    .inheritIO()
    .command(*command)
    .start()
    .waitFor()
    .also { check(it == 0) }
}
