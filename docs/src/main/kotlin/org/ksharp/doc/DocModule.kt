package org.ksharp.doc

data class DocAbstraction(
    val name: String,
    val representation: String,
    val documentation: String = ""
)

data class Trait(
    val name: String,
    val documentation: String = "",
    val abstractions: List<DocAbstraction>,
    val impls: List<String>
)

interface DocModule {
    fun representation(name: String, container: String = ""): String?

    fun documentation(name: String, container: String = ""): String?

    val traits: List<Trait>
    val abstractions: List<DocAbstraction>
}

internal data class MemoryDocModule internal constructor(
    override val traits: List<Trait>,
    override val abstractions: List<DocAbstraction>
) : DocModule {

    private fun List<DocAbstraction>.abstraction(name: String): DocAbstraction? =
        binarySearch {
            it.name.compareTo(name)
        }.let {
            if (it < 0) return null
            else this[it]
        }

    private fun List<Trait>.trait(name: String): Trait? =
        binarySearch {
            it.name.compareTo(name)
        }.let {
            if (it < 0) return null
            else this[it]
        }

    private fun abstractions(container: String): List<DocAbstraction> =
        if (container.isEmpty()) abstractions
        else traits.trait(container)?.abstractions ?: emptyList()

    override fun representation(name: String, container: String): String? =
        abstractions(container).abstraction(name)?.representation

    override fun documentation(name: String, container: String): String? =
        abstractions(container).abstraction(name)?.documentation
}

fun docModule(
    traits: List<Trait>,
    abstractions: List<DocAbstraction>
): DocModule =
    MemoryDocModule(
        traits.sortedBy {
            it.name
        },
        abstractions.sortedBy {
            it.name
        })
