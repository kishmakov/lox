package ru.kishmakov

class Scanner(private val source: String) {
    fun scanTokens(): List<Token> = source.split(" ").map { Token(it) }
}
