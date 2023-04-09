package org.ksharp.common.io

interface SerializerWriter<T> {
    fun write(input: T, buffer: BufferWriter, table: BinaryTable)

}

interface SerializerReader<T> {
    fun read(buffer: BufferView, table: BinaryTableView): T

}