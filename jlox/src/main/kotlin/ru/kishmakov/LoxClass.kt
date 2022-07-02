package ru.kishmakov

class LoxClass(val name: String, val methods: Map<String, LoxFunction>): LoxCallable {
    override val arity = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>) = LoxInstance(this)

    override fun toString() = name

    fun findMethod(name: String?): LoxFunction? {
        return if (methods.containsKey(name)) {
            methods[name]
        } else null
    }
}

class LoxInstance(private val klass: LoxClass) {
    private val fields = HashMap<String, Any?>()

    override fun toString(): String {
        return klass.name + " instance"
    }

    operator fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        klass.findMethod(name.lexeme)?.let { return it }

        throw RuntimeError(name, "Undefined property '" + name.lexeme + "'.")
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}