package ru.kishmakov

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess
import kotlin.text.Charsets.UTF_8


fun main(args: Array<String>) {
    when {
        args.isEmpty() -> {
            runPrompt()
        }
        args.size == 1 -> {
            runFile(args[0])
        }
        args.size > 1 -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }
    }
}


private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(bytes.toString(UTF_8))
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: List<Token> = scanner.scanTokens()

    // For now, just print the tokens.

    // For now, just print the tokens.
    for (token in tokens) {
        println(token.str)
    }
}

@Throws(IOException::class)
private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        run(line)
    }
}