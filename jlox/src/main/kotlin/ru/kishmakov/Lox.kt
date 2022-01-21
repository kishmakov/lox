package ru.kishmakov


sealed class RunResult {
    object Ok : RunResult()
    object Error: RunResult()
    object RuntimeError: RunResult()
}

class Lox {
    private var hadError = false
    private var hadRuntimeError = false
    private val interpreter = Interpreter(this)

    fun run(source: String): RunResult {
        val scanner = Scanner(source, this)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens, this)
        val expression = parser.parse()

        if (hadError) return RunResult.Error
        if (hadRuntimeError) return RunResult.RuntimeError

        interpreter.interpret(expression!!)
        // println(AstPrinter().print(expression!!))

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

    fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")

        hadError = true
    }
}