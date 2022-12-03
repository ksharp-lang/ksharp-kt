package org.ksharp.common

typealias Line = Int
typealias Offset = Int
typealias Position = Pair<Line, Offset>

data class Location(
    val context: String,
    val start: Position,
    val end: Position
)
