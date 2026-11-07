package com.example.culturama.data.profile

import com.example.culturama.data.remote.RespostaEuDto

/** Nome e interesses da conta do token, lidos e gravados pela API do site. */
data class CadastroDaConta(
    val nome: String,
    val interesses: List<String>
)

object CadastroRemoto {

    /** `null` quando a resposta não trouxe a conta. */
    fun ler(resposta: RespostaEuDto): CadastroDaConta? {
        val usuario = resposta.usuario ?: return null
        return CadastroDaConta(
            nome = usuario.nome?.trim().orEmpty(),
            interesses = usuario.interesses.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }
        )
    }
}
