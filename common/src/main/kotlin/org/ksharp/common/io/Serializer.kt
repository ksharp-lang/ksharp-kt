package org.ksharp.common.io

fun interface SerializerWriter<T> {
    fun write(input: T, buffer: BufferWriter, table: BinaryTable)

}

fun interface SerializerReader<T> {
    fun read(buffer: BufferView, table: BinaryTableView): T

}