package com.example.culturama.data.remote

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CategoriaDto(val id: String?, val nome: String?, val slug: String?)

/** Categoria do catálogo público, com as subcategorias em `filhas`. */
data class CategoriaArvoreDto(
    val id: String?,
    val nome: String?,
    val slug: String?,
    val filhas: List<CategoriaArvoreDto>? = emptyList()
)

data class ListaCategoriasDto(val categorias: List<CategoriaArvoreDto>?)
data class FonteDto(val nome: String?, val link: String?)
data class OrganizadorDto(val nome: String?, val verificado: Boolean?)

data class AcessibilidadeDto(
    val libras: Boolean? = false,
    val audiodescricao: Boolean? = false,
    val espaco: List<String>? = emptyList()
)

data class EventoResumoDto(
    val id: String,
    val titulo: String?,
    val dataHora: String?,
    val endereco: String?,
    val imagemCapa: String?,
    val gratuito: Boolean?,
    val acessivel: Boolean? = null,
    val categoria: CategoriaDto?,
    val fonte: FonteDto?
)

data class ListaEventosDto(val eventos: List<EventoResumoDto>?, val temMais: Boolean? = null)

data class EventoDetalheDto(
    val id: String,
    val titulo: String?,
    val dataHora: String?,
    val endereco: String?,
    val imagemCapa: String?,
    val gratuito: Boolean?,
    val acessivel: Boolean? = null,
    val categoria: CategoriaDto?,
    val fonte: FonteDto?,
    val descricao: String?,
    val linkIngresso: String?,
    val linkSaibaMais: String?,
    val organizador: OrganizadorDto?,
    val urlNoSite: String?,
    val acessibilidade: AcessibilidadeDto? = null
)

/** A API devolve o detalhe dentro de "evento". */
data class RespostaEventoDto(val evento: EventoDetalheDto)

data class UsuarioDto(
    val id: String? = null,
    val perfilCompleto: Boolean? = null,
    val nome: String? = null,
    val interesses: List<String>? = null,
    /** Definidos pelo servidor. Ausentes ou falsos não autorizam o painel. */
    val admin: Boolean? = null,
    val moderador: Boolean? = null,
    /** Papel gravado em `users.tipo` quando a API o envia no lugar dos booleanos. */
    val tipo: String? = null
)
data class RespostaEuDto(val usuario: UsuarioDto?)

/** Campo nulo não vai no JSON: o servidor só muda o que foi enviado. */
data class AtualizarContaBody(val nome: String? = null, val interesses: List<String>? = null)

data class CompletarPerfilBody(val dataNascimento: String, val aceite: Boolean)

data class CadastroBody(
    val nome: String,
    val email: String,
    val senha: String,
    val dataNascimento: String,
    val aceite: Boolean
)

data class FavoritoBody(val eventoId: String)

/** Rotas do site. O login não passa por aqui: é feito pelo SDK do Supabase. */
interface CulturamaApi {
    @GET("eventos")
    suspend fun eventos(
        @Query("de") de: String? = null,
        @Query("ate") ate: String? = null,
        @Query("limite") limite: Int? = null,
        @Query("pagina") pagina: Int? = null
    ): ListaEventosDto

    @GET("eventos/{id}")
    suspend fun evento(@Path("id") id: String): RespostaEventoDto

    @POST("cadastro")
    suspend fun cadastro(@Body body: CadastroBody)

    @GET("categorias")
    suspend fun categorias(): ListaCategoriasDto

    @GET("eu")
    suspend fun eu(): RespostaEuDto

    @PATCH("eu")
    suspend fun atualizarConta(@Body body: AtualizarContaBody): RespostaEuDto

    @POST("eu/completar")
    suspend fun completarPerfil(@Body body: CompletarPerfilBody)

    @GET("favoritos")
    suspend fun favoritos(): JsonElement

    @POST("favoritos")
    suspend fun favoritar(@Body body: FavoritoBody)

    @DELETE("favoritos/{eventoId}")
    suspend fun desfavoritar(@Path("eventoId") eventoId: String)
}
