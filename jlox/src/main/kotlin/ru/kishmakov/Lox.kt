package ru.kishmakov


sealed class RunResult {
    object Ok : RunResult()
    object Error: RunResult()
}

class Lox {
    fun run(source: String): RunResult {
        val scanner = Scanner(source, this)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens, this)
        val expression = parser.parse()

        if (hadError) return RunResult.Error

        println(AstPrinter().print(expression!!))

        return RunResult.Ok
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String?) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message!!)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message!!)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")

        hadError = true
    }

    var hadError = false
}