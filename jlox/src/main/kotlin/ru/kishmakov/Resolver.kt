package ru.kishmakov

class Resolver(private val interpreter: Interpreter, private val lox: Lox) :
    Expr.Visitor<Unit>,
    Stmt.Visitor<Unit> {

    private val scopes = ArrayList<HashMap<String, Boolean>>()
    private var currentFunction: FunctionType = FunctionType.NONE
    private var currentClass: ClassType = ClassType.NONE

    fun resolve(statements: List<Stmt>) = statements.forEach(::resolveStmt)

    private fun resolveStmt(stmt: Stmt) = stmt.accept(this)

    private fun resolveExpr(expr: Expr) = expr.accept(this)

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        function.params.forEach { param ->
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    private fun beginScope() = scopes.add(HashMap())

    private fun endScope() = scopes.removeLast()

    private fun declare(name: Token) = scopes.lastOrNull()?.also { scope ->
        if (name.lexeme in scope.keys) {
            lox.error(name,"Already a variable with this name in this scope.")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) = scopes.lastOrNull()?.also { it[name.lexeme] = true }

    private fun resolveLocal(expr: Expr, name: Token) {
        scopes.indices.reversed().forEach { i ->
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolveExpr(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        resolveExpr(expr.left)
        resolveExpr(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call) {
        resolveExpr(expr.callee)
        expr.arguments.forEach { resolveExpr(it) }
    }

    override fun visitGetExpr(expr: Expr.Get) {
        resolveExpr(expr.obj)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) = resolveExpr(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal) {}

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolveExpr(expr.left)
        resolveExpr(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set) {
        resolveExpr(expr.value)
        resolveExpr(expr.obj)
    }

    override fun visitThisExpr(expr: Expr.This) {
        if (currentClass == ClassType.NONE) {
            lox.error(expr.keyword, "Can't use 'this' outside of a class.")
        }

        resolveLocal(expr, expr.keyword)
    }

    override fun visitUnaryExpr(expr: Expr.Unary) = resolveExpr(expr.right)

    override fun visitVariableExpr(expr: Expr.Variable) {
        if (scopes.lastOrNull()?.get(expr.name.lexeme) == false) {
            lox.error(expr.name, "Can't read local variable in its own initializer.")
        }

        resolveLocal(expr, expr.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name)
        beginScope();
        scopes.lastOrNull()?.put("this", true)

        for (method in stmt.methods) {
            val declaration: FunctionType = FunctionType.METHOD
            resolveFunction(method, declaration)
        }

        define(stmt.name)
        endScope()
        currentClass = enclosingClass
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) = resolveExpr(stmt.expression)

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolveExpr(stmt.condition)
        resolveStmt(stmt.thenBranch)
        stmt.elseBranch?.also(::resolveStmt)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) = resolveExpr(stmt.expression)

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunction == FunctionType.NONE) {
            lox.error(stmt.keyword, "Can't return from top-level code.")
        }

        stmt.value?.also(::resolveExpr)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        stmt.initializer?.also(::resolveExpr)
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        resolveExpr(stmt.condition)
        resolveStmt(stmt.body)
    }
}

private enum class FunctionType {
    NONE,
    FUNCTION,
    METHOD
}

private enum class ClassType {
    NONE, CLASS
}