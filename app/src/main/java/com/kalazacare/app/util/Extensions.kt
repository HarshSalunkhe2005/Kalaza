package com.kalazacare.app.util

fun String.toInitials(): String =
    this.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

fun String.capitalizeWords(): String =
    this.lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercaseChar() }
    }
