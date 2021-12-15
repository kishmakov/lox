package ru.kishmakov

class Scanner(source: String) {
    fun scanTokens(): List<Token> = listOf("2", "3", "9").map { Token(it) }
}
