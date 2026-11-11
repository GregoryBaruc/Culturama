package com.example.culturama.data.acesso

import com.example.culturama.BuildConfig
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
 * Em `organizers` a consulta encontra id, user_id, documento, tipo_documento e observacao.
 * Não há colunas de nome do coletivo, logo, descrição, endereço ou contato.
 * Não há tabela de colaboradores. O número do documento não é pedido nem exibido.
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
}

data class PerfilDoMural(
    val id: String,
    val nomeDoResponsavel: String?,
    val tipoDocumento: String?,
    val observacao: String?
)

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
    @SerializedName("tipo_documento") val tipoDocumento: String? = null,
    val observacao: String? = null,
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
        return PerfilDoMural(
            id = linha.id!!.trim(),
            nomeDoResponsavel = linha.user?.nome?.trim()?.takeIf { it.isNotEmpty() },
            tipoDocumento = linha.tipoDocumento?.trim()?.takeIf { it.isNotEmpty() },
            observacao = linha.observacao?.trim()?.takeIf { it.isNotEmpty() }
        )
    }
    return null
}

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
