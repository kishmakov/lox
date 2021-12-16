package ru.kishmakov

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

@Throws(IOException::class)
fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        Lox.instance.run(line)
    }
}

@Throws(IOException::class)
fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    val result = Lox.instance.run(bytes.toString(Charsets.UTF_8))

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
