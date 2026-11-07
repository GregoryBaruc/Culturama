package com.example.culturama.data

/** Uma página da lista: os itens e se o servidor ainda tem mais. */
internal data class Pagina<T>(val itens: List<T>, val temMais: Boolean)

/**
 * Pede uma página depois da outra até o servidor dizer que acabou.
 * Página vazia ou o teto de páginas também param, para nunca ficar em repetição infinita.
 * Se uma página falhar, a falha sobe: é melhor pedir para tentar de novo do que mostrar uma lista incompleta como se fosse tudo.
 */
internal suspend fun <T> buscarTodasAsPaginas(
    maxPaginas: Int = 50,
    buscarPagina: suspend (numero: Int) -> Pagina<T>
): List<T> {
    val todos = mutableListOf<T>()
    var numero = 1
    while (numero <= maxPaginas) {
        val pagina = buscarPagina(numero)
        todos += pagina.itens
        if (!pagina.temMais || pagina.itens.isEmpty()) break
        numero++
    }
    return todos
}
