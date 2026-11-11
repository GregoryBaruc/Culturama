package com.example.culturama.data.acesso

import com.example.culturama.BuildConfig
import com.example.culturama.data.admin.AcessoAdministrativo
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.lerInstante
import com.example.culturama.data.remote.SupabaseProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Leitura do espaço do organizador da sessão.
 * Usa o JWT do usuário e a chave pública. Não grava, não cria vínculo e não usa service_role.
 *
 * Em `organizers` a consulta pede id, verificado, status, tipo_documento, observacao e,
 * se a coluna existir, created_at. O número em `documento` não é pedido nem exibido.
 * Não há colunas de nome do coletivo, logo, descrição, endereço ou contato.
 * Não há tabela de colaboradores nem envio de convite.
 */
object MuralDoOrganizador {

    suspend fun carregar(): CargaDoMural = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseProvider.client?.auth?.currentUserOrNull()?.id?.trim().orEmpty()
            if (userId.isEmpty()) return@withContext CargaDoMural.Falhou("Entre de novo para abrir o espaço cultural.")
            val perfil = lerPerfil(userId) ?: return@withContext CargaDoMural.SemCadastro
            val eventos = lerEventos(perfil.id)
            CargaDoMural.Pronta(perfil, eventos)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            CargaDoMural.Falhou("Não foi possível abrir o espaço cultural.")
        }
    }

    /**
     * Abre outra organização só se a sessão for administrador.
     * A leitura usa o JWT da sessão. Se o RLS não devolver a linha, o mural não inventa dados.
     */
    suspend fun carregarComoAdministrador(organizacaoId: String): CargaDoMural = withContext(Dispatchers.IO) {
        try {
            if (!ehAdministrador()) {
                return@withContext CargaDoMural.Falhou("Sem permissão para abrir outra organização.")
            }
            val perfil = lerPerfilPorId(organizacaoId.trim()) ?: return@withContext CargaDoMural.SemCadastro
            CargaDoMural.Pronta(perfil, lerEventos(perfil.id))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            CargaDoMural.Falhou("Não foi possível abrir esta organização.")
        }
    }

    /** Organizações que o JWT do administrador consegue ler. Lista vazia não cria cadastro. */
    suspend fun listarParaAdministrador(): Result<List<ResumoDeOrganizacao>> = withContext(Dispatchers.IO) {
        if (!ehAdministrador()) {
            return@withContext Result.failure(IllegalStateException("Sem permissão para listar organizações."))
        }
        val selects = listOf(
            "id,verificado,status,user:users(nome)",
            "id,verificado,status"
        )
        for (select in selects) {
            val resposta = pedir("organizers", "select=${enc(select)}&limit=50")
            if (resposta.codigo == 400) continue
            if (resposta.codigo !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("A sessão não autorizou a lista de organizações.")
                )
            }
            val linhas = Gson().fromJson(resposta.corpo, Array<LinhaPerfil>::class.java).orEmpty()
            return@withContext Result.success(
                linhas.mapNotNull { linha ->
                    val id = linha.id?.trim().orEmpty()
                    if (id.isEmpty()) null
                    else ResumoDeOrganizacao(
                        id = id,
                        rotulo = rotuloDeOrganizacao(linha.user?.nome, id),
                        situacao = situacaoCadastral(linha.verificado, linha.status).titulo
                    )
                }.distinctBy { it.id }
            )
        }
        Result.failure(IllegalStateException("Não foi possível ler as organizações."))
    }
}

data class ResumoDeOrganizacao(
    val id: String,
    val rotulo: String,
    val situacao: String
)

/** Nome real do responsável, quando a consulta traz. Sem nome, um rótulo neutro com um trecho do id. */
fun rotuloDeOrganizacao(nome: String?, id: String): String {
    val limpo = nome?.trim()?.takeIf { it.isNotEmpty() }
    if (limpo != null) return limpo
    val trecho = id.trim().take(4).ifBlank { "----" }
    return "Organização sem nome · $trecho"
}

private suspend fun ehAdministrador(): Boolean =
    AcessoAdministrativo.verificar().getOrNull() == PapelNoPainel.ADMINISTRADOR

data class PerfilDoMural(
    val id: String,
    val nomeDoResponsavel: String?,
    val tipoDocumento: String?,
    val observacao: String?,
    val verificado: Boolean? = null,
    val status: String? = null,
    /** `created_at` quando a coluna existe. Não é data de aprovação. */
    val criadoEm: String? = null
)

/** Apresentação da verificação. Só mapeia marcas que o servidor já usa. */
enum class SituacaoCadastral(val titulo: String, val detalhe: String) {
    EM_ANALISE(
        "Em análise",
        "O cadastro foi enviado e aguarda avaliação da plataforma."
    ),
    APROVADO(
        "Aprovado",
        "O cadastro está verificado pela plataforma."
    ),
    AJUSTES(
        "Ajustes necessários",
        "O servidor pediu correção nas informações deste cadastro."
    ),
    RECUSADO(
        "Recusado",
        "A solicitação não foi aprovada."
    ),
    SEM_CONFIRMACAO(
        "Sem confirmação",
        "O servidor não informou a situação deste cadastro. Nada foi marcado como aprovado."
    )
}

fun situacaoCadastral(verificado: Boolean?, status: String?): SituacaoCadastral {
    val texto = status?.trim()?.lowercase()
        ?.replace("ç", "c")
        ?.replace("ã", "a")
        ?.replace("á", "a")
        .orEmpty()
    val pedeAjuste = texto in setOf("ajuste", "ajustes", "correcao", "correcao_necessaria")
    return when {
        pedeAjuste -> SituacaoCadastral.AJUSTES
        verificado == true || statusAutorizado(status) -> SituacaoCadastral.APROVADO
        statusPendente(status) -> SituacaoCadastral.EM_ANALISE
        statusRecusado(status) -> SituacaoCadastral.RECUSADO
        else -> SituacaoCadastral.SEM_CONFIRMACAO
    }
}

data class EventoDoMural(
    val id: String,
    val titulo: String,
    val status: String?,
    val dataHora: String?,
    val dataFim: String? = null,
    val descricao: String? = null,
    val endereco: String? = null,
    val imagemCapa: String? = null,
    val categoriaId: String? = null,
    val categoriaNome: String? = null
)

/** Filtros da lista do organizador. Encerrado usa `data_fim`, porque não existe status com esse nome. */
enum class FiltroDosEventos(val rotulo: String) {
    TODOS("Todos"),
    PUBLICADOS("Publicados"),
    EM_ANALISE("Em análise"),
    RASCUNHOS("Rascunhos"),
    ENCERRADOS("Encerrados")
}

data class ContagemDoMural(
    val publicados: Int,
    val emAnalise: Int,
    val rascunhos: Int
)

sealed interface CargaDoMural {
    data class Pronta(
        val perfil: PerfilDoMural,
        /** Nulo quando a leitura dos eventos foi recusada ou falhou. Lista vazia é uma consulta vazia. */
        val eventos: List<EventoDoMural>?
    ) : CargaDoMural

    data object SemCadastro : CargaDoMural
    data class Falhou(val mensagem: String) : CargaDoMural
}

fun List<EventoDoMural>.contagem(): ContagemDoMural {
    var publicados = 0
    var emAnalise = 0
    var rascunhos = 0
    for (evento in this) {
        when (normalizarStatus(evento.status)) {
            "publicado" -> publicados++
            "em_revisao" -> emAnalise++
            "rascunho" -> rascunhos++
        }
    }
    return ContagemDoMural(publicados, emAnalise, rascunhos)
}

fun List<EventoDoMural>.filtrar(
    filtro: FiltroDosEventos,
    busca: String,
    agoraMillis: Long
): List<EventoDoMural> {
    val termo = semAcento(busca)
    return filter { evento ->
        val nomeOk = termo.isEmpty() || semAcento(evento.titulo).contains(termo)
        val statusOk = when (filtro) {
            FiltroDosEventos.TODOS -> true
            FiltroDosEventos.PUBLICADOS -> normalizarStatus(evento.status) == "publicado"
            FiltroDosEventos.EM_ANALISE -> normalizarStatus(evento.status) == "em_revisao"
            FiltroDosEventos.RASCUNHOS -> normalizarStatus(evento.status) == "rascunho"
            FiltroDosEventos.ENCERRADOS -> {
                val fim = lerInstante(evento.dataFim) ?: return@filter false
                fim < agoraMillis
            }
        }
        nomeOk && statusOk
    }
}

fun rotuloDoStatus(status: String?): String = when (normalizarStatus(status)) {
    "" -> "Sem status"
    "publicado" -> "Publicado"
    "rascunho" -> "Rascunho"
    "em_revisao" -> "Em análise"
    "cancelado" -> "Cancelado"
    "inativo" -> "Inativo"
    else -> status?.trim().orEmpty().ifBlank { "Sem status" }
}

private fun normalizarStatus(status: String?): String =
    status?.trim()?.lowercase()?.replace(' ', '_').orEmpty()

private fun semAcento(texto: String): String =
    java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
        .trim()

private data class LinhaPerfil(
    val id: String? = null,
    val verificado: Boolean? = null,
    val status: String? = null,
    @SerializedName("tipo_documento") val tipoDocumento: String? = null,
    val observacao: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val user: LinhaNome? = null
)

private data class LinhaNome(val nome: String? = null)

private data class LinhaCategoria(val nome: String? = null)

private data class LinhaEvento(
    val id: String? = null,
    val titulo: String? = null,
    val status: String? = null,
    val descricao: String? = null,
    val endereco: String? = null,
    @SerializedName("data_hora") val dataHora: String? = null,
    @SerializedName("data_fim") val dataFim: String? = null,
    @SerializedName("imagem_capa") val imagemCapa: String? = null,
    @SerializedName("categoria_id") val categoriaId: String? = null,
    val categoria: LinhaCategoria? = null
)

private fun lerPerfil(userId: String): PerfilDoMural? {
    val selects = listOf(
        "id,verificado,status,tipo_documento,observacao,created_at,user:users(nome)",
        "id,verificado,status,tipo_documento,observacao,user:users(nome)",
        "id,tipo_documento,observacao,user:users(nome)",
        "id,tipo_documento,observacao",
        "id"
    )
    for (select in selects) {
        val resposta = pedir("organizers", "user_id=eq.${enc(userId)}&select=${enc(select)}&limit=1")
        if (resposta.codigo == 400) continue
        if (resposta.codigo !in 200..299) return null
        val linhas = Gson().fromJson(resposta.corpo, Array<LinhaPerfil>::class.java) ?: return null
        val linha = linhas.firstOrNull { !it.id.isNullOrBlank() } ?: return null
        return perfilDe(linha)
    }
    return null
}

private fun lerPerfilPorId(organizacaoId: String): PerfilDoMural? {
    if (organizacaoId.isBlank()) return null
    val selects = listOf(
        "id,verificado,status,tipo_documento,observacao,created_at,user:users(nome)",
        "id,verificado,status,tipo_documento,observacao,user:users(nome)",
        "id,tipo_documento,observacao",
        "id"
    )
    for (select in selects) {
        val resposta = pedir(
            "organizers",
            "id=eq.${enc(organizacaoId)}&select=${enc(select)}&limit=1"
        )
        if (resposta.codigo == 400) continue
        if (resposta.codigo !in 200..299) return null
        val linhas = Gson().fromJson(resposta.corpo, Array<LinhaPerfil>::class.java) ?: return null
        val linha = linhas.firstOrNull { !it.id.isNullOrBlank() } ?: return null
        return perfilDe(linha)
    }
    return null
}

private fun perfilDe(linha: LinhaPerfil) = PerfilDoMural(
    id = linha.id!!.trim(),
    nomeDoResponsavel = linha.user?.nome?.trim()?.takeIf { it.isNotEmpty() },
    tipoDocumento = linha.tipoDocumento?.trim()?.takeIf { it.isNotEmpty() },
    observacao = linha.observacao?.trim()?.takeIf { it.isNotEmpty() },
    verificado = linha.verificado,
    status = linha.status?.trim()?.takeIf { it.isNotEmpty() },
    criadoEm = linha.createdAt?.trim()?.takeIf { it.isNotEmpty() }
)

private fun lerEventos(organizacaoId: String): List<EventoDoMural>? {
    val selects = listOf(
        "id,titulo,descricao,status,data_hora,data_fim,endereco,imagem_capa,categoria_id,categoria:categories(nome)",
        "id,titulo,status,data_hora"
    )
    for (select in selects) {
        val filtro = "organizer_id=eq.${enc(organizacaoId)}&select=${enc(select)}" +
            "&order=data_hora.desc.nullslast&limit=100"
        val resposta = pedir("events", filtro)
        if (resposta.codigo == 400) continue
        if (resposta.codigo !in 200..299) return null
        val linhas = Gson().fromJson(resposta.corpo, Array<LinhaEvento>::class.java) ?: return null
        return linhas.mapNotNull { it.paraEvento() }
    }
    return null
}

private fun LinhaEvento.paraEvento(): EventoDoMural? {
    val identificador = id?.trim().orEmpty()
    if (identificador.isEmpty()) return null
    val capa = imagemCapa?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    return EventoDoMural(
        id = identificador,
        titulo = titulo?.trim().orEmpty().ifBlank { "Sem título" },
        status = status,
        dataHora = dataHora,
        dataFim = dataFim,
        descricao = descricao?.trim()?.takeIf { it.isNotEmpty() },
        endereco = endereco?.trim()?.takeIf { it.isNotEmpty() },
        imagemCapa = capa,
        categoriaId = categoriaId?.trim()?.takeIf { it.isNotEmpty() },
        categoriaNome = categoria?.nome?.trim()?.takeIf { it.isNotEmpty() }
    )
}

private data class Resposta(val codigo: Int, val corpo: String)

private fun pedir(tabela: String, consulta: String): Resposta {
    val token = SupabaseProvider.client?.auth?.currentAccessTokenOrNull()?.trim().orEmpty()
    val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    if (token.isEmpty() || chave.isBlank()) return Resposta(401, "")
    val url = URL("${BuildConfig.SUPABASE_URL}/rest/v1/$tabela?$consulta")
    val conexao = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8000
        readTimeout = 8000
        setRequestProperty("apikey", chave)
        setRequestProperty("Authorization", "Bearer $token")
        setRequestProperty("Accept", "application/json")
    }
    return try {
        val codigo = conexao.responseCode
        val fluxo = if (codigo in 200..299) conexao.inputStream else conexao.errorStream
        val corpo = fluxo?.bufferedReader()?.use { it.readText() }.orEmpty()
        Resposta(codigo, if (codigo in 200..299 && corpo.isBlank()) "[]" else corpo)
    } catch (_: Exception) {
        Resposta(0, "")
    } finally {
        conexao.disconnect()
    }
}

private fun enc(valor: String): String = URLEncoder.encode(valor, "UTF-8")
