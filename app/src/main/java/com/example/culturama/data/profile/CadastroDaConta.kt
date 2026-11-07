package com.example.culturama.data.profile

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/** Linha da própria conta em `public.users`. Só nome e interesses podem ser alterados aqui. */
data class CadastroDaConta(
    val nome: String,
    val interesses: List<String>
)

data class LinhaUsuarioDto(
    @SerializedName("auth_id") val authId: String? = null,
    val nome: String? = null,
    val interesses: List<String>? = null
)

/**
 * Monta e confere as chamadas da tabela `users`.
 * O filtro usa somente o id da sessão. A resposta precisa devolver esse mesmo id.
 */
object CadastroRemoto {

    private val gson = Gson()
    private val idDaSessao = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun idValido(authId: String): Boolean = idDaSessao.matches(authId)

    /** Consulta fixa: não inclui texto digitado pela pessoa. */
    fun caminhoDaPropriaConta(authId: String): String? {
        if (!idValido(authId)) return null
        return "users?select=nome,interesses,auth_id&auth_id=eq.$authId&limit=1"
    }

    fun corpoDoNome(nome: String): String = gson.toJson(mapOf("nome" to nome))

    fun corpoDosInteresses(tokens: List<String>): String = gson.toJson(mapOf("interesses" to tokens))

    /** `null` quando a resposta não é a linha desta conta, ou quando veio vazia. */
    fun ler(json: String, authId: String): CadastroDaConta? {
        val linhas = decodificar(json) ?: return null
        if (linhas.size != 1 || linhas[0].authId != authId) return null
        return CadastroDaConta(
            nome = linhas[0].nome?.trim().orEmpty(),
            interesses = linhas[0].interesses.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    fun confirmarNome(json: String, authId: String, nomeEsperado: String): Boolean {
        val gravado = ler(json, authId) ?: return false
        return gravado.nome == nomeEsperado
    }

    fun confirmarInteresses(json: String, authId: String, tokens: List<String>): Boolean {
        val gravado = ler(json, authId) ?: return false
        return gravado.interesses.toSet() == tokens.toSet()
    }

    private fun decodificar(json: String): List<LinhaUsuarioDto>? {
        val texto = json.trim()
        if (!texto.startsWith("[")) return null
        return runCatching { gson.fromJson(texto, Array<LinhaUsuarioDto>::class.java)?.toList() }.getOrNull()
    }
}
