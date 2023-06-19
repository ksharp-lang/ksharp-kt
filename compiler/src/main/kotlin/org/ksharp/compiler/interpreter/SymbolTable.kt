package org.ksharp.compiler.interpreter

fun interface SymbolTable {
    operator fun get(name: String): Any?

}

interface OpenSymbolTable : SymbolTable {
    operator fun set(name: String, value: Any): Boolean

}

fun readOnlySymbolTable(symbols: Map<String, Any>) = SymbolTable {
    symbols[it]
}

fun openSymbolTable() = object : OpenSymbolTable {
    private val symbols = mutableMapOf<String, Any>()

    override fun set(name: String, value: Any): Boolean =
        if (symbols.containsKey(name)) false
        else {
            symbols[name] = value
            true
        }

    override fun get(name: String): Any? = symbols[name]
}

fun chainSymbolTables(first: SymbolTable, second: SymbolTable) =
    SymbolTable {
        first[it] ?: second[it]
    }
