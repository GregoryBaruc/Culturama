package com.example.culturama.data.profile

import com.example.culturama.data.remote.CategoriaArvoreDto
import com.example.culturama.data.remote.ListaCategoriasDto

/** Subcategoria, ou a própria categoria quando ela não tem filhas. */
data class OpcaoCultural(
    val id: String,
    val nome: String,
    val slug: String
)

/** Grupo exibido na tela, na ordem em que o catálogo do site devolve. */
data class GrupoCultural(
    val id: String,
    val nome: String,
    val slug: String,
    val opcoes: List<OpcaoCultural>
)

/**
 * Converte o JSON de GET /api/v1/categorias.
 * Categorias com filhas viram o título do grupo; as filhas viram os chips.
 * Categorias sem filhas viram um chip com o próprio nome.
 */
object CatalogoCultural {

    fun grupos(resposta: ListaCategoriasDto): List<GrupoCultural> {
        return resposta.categorias.orEmpty().mapNotNull { it.paraGrupo() }
    }

    private fun CategoriaArvoreDto.paraGrupo(): GrupoCultural? {
        val idGrupo = id?.trim().orEmpty()
        val nomeGrupo = nome?.trim().orEmpty()
        val slugGrupo = slug?.trim().orEmpty()
        if (idGrupo.isEmpty() || nomeGrupo.isEmpty() || slugGrupo.isEmpty()) return null

        val filhasValidas = filhas.orEmpty().mapNotNull { it.paraOpcao() }
        val opcoes = filhasValidas.ifEmpty {
            listOf(OpcaoCultural(idGrupo, nomeGrupo, slugGrupo))
        }
        return GrupoCultural(idGrupo, nomeGrupo, slugGrupo, opcoes)
    }

    private fun CategoriaArvoreDto.paraOpcao(): OpcaoCultural? {
        val idOpcao = id?.trim().orEmpty()
        val nomeOpcao = nome?.trim().orEmpty()
        val slugOpcao = slug?.trim().orEmpty()
        if (idOpcao.isEmpty() || nomeOpcao.isEmpty() || slugOpcao.isEmpty()) return null
        return OpcaoCultural(idOpcao, nomeOpcao, slugOpcao)
    }
}
