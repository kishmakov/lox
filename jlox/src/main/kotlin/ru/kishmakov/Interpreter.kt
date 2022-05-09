package ru.kishmakov

import ru.kishmakov.Expr.Assign
import ru.kishmakov.Expr.Logical
import ru.kishmakov.Stmt.While


class Interpreter(private val lox: Lox) :
    Expr.Visitor<Any?>,
    Stmt.Visitor<Unit> {

    val globals = Environment()
    private var environment = globals

    init {
        globals.define("clock", object : LoxCallable {
            override val arity = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Double {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString() = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            lox.runtimeError(error)
        }
    }

    private fun execute(statement: Stmt) {
        statement.accept(this)
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) <= (right as Double)
            }
            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                return when {
                    left is Double && right is Double -> left + right
                    left is String && right is String -> left + right
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }
        }

        return null // Unreachable.
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val function = evaluate(expr.callee) as? LoxCallable ?:
            throw RuntimeError(expr.paren, "Can only call functions and classes.")

        if (expr.arguments.size != function.arity) {
            throw RuntimeError(
                expr.paren, "Expected ${function.arity} arguments but got " +
                        expr.arguments.size.toString() + "."
            )
        }

        val arguments = expr.arguments.map { evaluate(it) }

        return function.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): Any? = expr.value

    override fun visitLogicalExpr(expr: Logical): Any? {
        val left = evaluate(expr.left)

        return when {
            expr.operator.type == TokenType.OR && isTruthy(left) -> left
            expr.operator.type == TokenType.AND && !isTruthy(left) -> left
            else -> evaluate(expr.right)
        }
    }


    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(right as Double)
            }
        }

        return null // Unreachable.
    }

    override fun visitVariableExpr(expr: Expr.Variable) = environment[expr.name]

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt)
        environment.define(stmt.name.lexeme, function)
    }
    
    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = when (stmt.initializer) {
            null -> null
            else -> evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    private fun isTruthy(right: Any?): Boolean = when (right) {
        null, false -> false
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        return a?.equals(b) ?: false
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }


    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return

        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(obj: Any?): String = when (obj) {
        null -> "nil"
        is Double -> {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            text
        }
        else -> obj.toString()
    }
}

class RuntimeError(val token: Token, message: String?) : RuntimeException(message)

class Environment {
    private val enclosing: Environment?

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Environment) {
        this.enclosing = enclosing
    }

    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) { values[name] = value }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) {
            return enclosing[name]
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}
