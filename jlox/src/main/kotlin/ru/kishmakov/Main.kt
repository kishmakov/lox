package ru.kishmakov

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

@Throws(IOException::class)
fun runPrompt() {
    val lox = Lox()
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        lox.run(line)
    }
}

@Throws(IOException::class)
fun runFile(path: String) {
    val lox = Lox()
    val bytes = Files.readAllBytes(Paths.get(path))
    val result = lox.run(bytes.toString(Charsets.UTF_8))

    if (result == RunResult.Error) exitProcess(65)
}


fun main(args: Array<String>) {
    when {
        args.isEmpty() -> runPrompt()
        args.size == 1 -> runFile(args[0])
        args.size > 1 -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }
    }
}
