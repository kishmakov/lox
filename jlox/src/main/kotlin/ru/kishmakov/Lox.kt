package ru.kishmakov

sealed class RunResult {
    object Ok : RunResult()
    object Error: RunResult()
}

class Lox {
    fun run(source: String): RunResult {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()

        if (hadError) return RunResult.Error

        // For now, just print the tokens.

        // For now, just print the tokens.
        for (token in tokens) {
            println(token.str)
        }

        return RunResult.Ok
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")

        hadError = true
    }

    companion object {
        var hadError = false

        val instance = Lox()
    }
}