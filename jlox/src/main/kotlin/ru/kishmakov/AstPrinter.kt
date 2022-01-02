package ru.kishmakov

internal class AstPrinter : Visitor<String> {
    fun print(expr: Expr): String = expr.accept(this)

    override fun visitBinaryExpr(expr: Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpr(expr: Grouping) = parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Literal) = expr.value?.toString() ?: "nil"

    override fun visitUnaryExpr(expr: Unary) = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr) = StringBuilder().apply {
        append("(").append(name)
        for (expr in exprs) {
            append(" ")
            append(expr.accept(this@AstPrinter))
        }
        append(")")
    }.toString()
}