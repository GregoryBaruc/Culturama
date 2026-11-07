package com.example.culturama.data.profile

/**
 * A coluna `users.interesses` é uma lista de texto.
 * Valores já gravados podem ser o id ou o slug da categoria.
 * A gravação repete o formato que a conta já usa, para não misturar os dois.
 */
object Interesses {

    fun opcoes(grupos: List<GrupoCultural>): List<OpcaoCultural> = grupos.flatMap { it.opcoes }

    /** Ids das opções que correspondem aos valores já salvos. */
    fun selecionados(salvos: List<String>, grupos: List<GrupoCultural>): Set<String> {
        val lista = opcoes(grupos)
        val porId = lista.associateBy { it.id }
        val porSlug = lista.associateBy { it.slug }
        return salvos.mapNotNull { token ->
            porId[token]?.id ?: porSlug[token]?.id
        }.toSet()
    }

    fun nomesVisiveis(salvos: List<String>, grupos: List<GrupoCultural>): List<String> {
        val escolhidos = selecionados(salvos, grupos)
        return opcoes(grupos).filter { it.id in escolhidos }.map { it.nome }
    }

    /**
     * Lista pronta para gravar. Conta nova, ou conta que já usa ids, grava ids.
     * Conta que já usa slugs continua gravando slugs.
     */
    fun tokensParaGravar(
        selecionados: Set<String>,
        salvosAntes: List<String>,
        grupos: List<GrupoCultural>
    ): List<String> {
        val lista = opcoes(grupos)
        val usarSlug = formatoExistenteEhSlug(salvosAntes, lista)
        return lista
            .filter { it.id in selecionados }
            .map { if (usarSlug) it.slug else it.id }
            .distinct()
    }

    fun resumo(nomes: List<String>): String? = when {
        nomes.isEmpty() -> null
        nomes.size <= 3 -> nomes.joinToString(", ")
        else -> nomes.take(3).joinToString(", ") + " e mais ${nomes.size - 3}"
    }

    private fun formatoExistenteEhSlug(salvos: List<String>, opcoes: List<OpcaoCultural>): Boolean {
        val ids = opcoes.map { it.id }.toSet()
        val slugs = opcoes.map { it.slug }.toSet()
        val reconhecidos = salvos.map { it.trim() }.filter { it in ids || it in slugs }
        if (reconhecidos.isEmpty()) return false
        val soSlug = reconhecidos.count { it in slugs && it !in ids }
        val comoId = reconhecidos.count { it in ids }
        return soSlug > comoId
    }
}
