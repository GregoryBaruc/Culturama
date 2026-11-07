package com.example.culturama.data.profile

/**
 * O site guarda slugs e pode trocar todas as filhas de um grupo pela categoria-mãe.
 * Valores antigos podem ser o id. A leitura aceita as três formas; a gravação manda slugs.
 */
object Interesses {

    fun opcoes(grupos: List<GrupoCultural>): List<OpcaoCultural> = grupos.flatMap { it.opcoes }

    /** Ids das opções que correspondem aos valores já salvos. A categoria-mãe marca todas as filhas. */
    fun selecionados(salvos: List<String>, grupos: List<GrupoCultural>): Set<String> {
        val lista = opcoes(grupos)
        val porId = lista.associateBy { it.id }
        val porSlug = lista.associateBy { it.slug }
        return salvos.flatMap { token ->
            val opcao = porId[token] ?: porSlug[token]
            if (opcao != null) {
                listOf(opcao.id)
            } else {
                grupos.firstOrNull { it.id == token || it.slug == token }?.opcoes?.map { it.id }.orEmpty()
            }
        }.toSet()
    }

    fun nomesVisiveis(salvos: List<String>, grupos: List<GrupoCultural>): List<String> {
        val escolhidos = selecionados(salvos, grupos)
        return opcoes(grupos).filter { it.id in escolhidos }.map { it.nome }
    }

    /** Lista pronta para gravar: o servidor deixa na forma canônica e devolve o que gravou. */
    fun tokensParaGravar(selecionados: Set<String>, grupos: List<GrupoCultural>): List<String> =
        opcoes(grupos).filter { it.id in selecionados }.map { it.slug }.distinct()

    fun resumo(nomes: List<String>): String? = when {
        nomes.isEmpty() -> null
        nomes.size <= 3 -> nomes.joinToString(", ")
        else -> nomes.take(3).joinToString(", ") + " e mais ${nomes.size - 3}"
    }
}
