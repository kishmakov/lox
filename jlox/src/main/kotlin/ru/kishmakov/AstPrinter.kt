package ru.kishmakov


internal class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr): String = expr.accept(this)

    override fun visitAssignExpr(expr: Expr.Assign): String {
        TODO("Not yet implemented")
    }

    override fun visitBinaryExpr(expr: Expr.Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitCallExpr(expr: Expr.Call): String {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) = parenthesize("group", expr.expression)

    override fun visitLambdaExpr(expr: Expr.Lambda): String {
        TODO("Not yet implemented")
    }

    override fun visitLiteralExpr(expr: Expr.Literal) = expr.value?.toString() ?: "nil"

    override fun visitUnaryExpr(expr: Expr.Unary) = parenthesize(expr.operator.lexeme, expr.right)

    override fun visitVariableExpr(expr: Expr.Variable): String {
        TODO("Not yet implemented")
    }

    private fun parenthesize(name: String, vararg exprs: Expr) = StringBuilder().apply {
        append("(").append(name)
        for (expr in exprs) {
            append(" ")
            append(expr.accept(this@AstPrinter))
        }
        append(")")
    }.toString()

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("Not yet implemented")
    }
}