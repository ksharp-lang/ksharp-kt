package org.ksharp.common

@JvmInline
value class Line(val value: Int)

@JvmInline
value class Offset(val value: Int)

typealias Position = Pair<Line, Offset>

data class Location(
    val context: String,
    val position: Position,
) {
    companion object {
        val NoProvided = Location("", Line(0) to Offset(0))
    }
}