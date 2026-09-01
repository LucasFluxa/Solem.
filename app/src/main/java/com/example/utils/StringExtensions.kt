package com.example.utils

fun String.toTitleCase(): String {
    if (this.isBlank()) return this
    val exceptions = setOf(
        "de", "la", "el", "en", "y", "o", "u", "a", "los", "las", "del", "por", "para", "con", "sin", "un", "una", "unos", "unas"
    )
    return this.trim().lowercase().split(" ").joinToString(" ") { word ->
        if (word in exceptions) word else word.replaceFirstChar { it.uppercase() }
    }.replace(Regex("\\bIii\\b"), "III")
     .replace(Regex("\\bIi\\b"), "II")
     .replace(Regex("\\bIv\\b"), "IV")
     .replace(Regex("\\bVi\\b"), "VI")
     .replace(Regex("\\bVii\\b"), "VII")
     .replace(Regex("\\bViii\\b"), "VIII")
     .replace(Regex("\\bIx\\b"), "IX")
     .replace(Regex("\\bI\\b"), "I")
     .replace(Regex("\\bV\\b"), "V")
     .replace(Regex("\\bX\\b"), "X")
}
