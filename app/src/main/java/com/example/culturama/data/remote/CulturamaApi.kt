package com.example.culturama.data.remote

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CategoriaDto(val id: String?, val nome: String?, val slug: String?)
data class FonteDto(val nome: String?, val link: String?)
data class OrganizadorDto(val nome: String?, val verificado: Boolean?)

data class EventoResumoDto(
    val id: String,
    val titulo: String?,
    val dataHora: String?,
    val endereco: String?,
    val imagemCapa: String?,
    val gratuito: Boolean?,
    val categoria: CategoriaDto?,
    val fonte: FonteDto?
)

data class ListaEventosDto(val eventos: List<EventoResumoDto>?)

data class EventoDetalheDto(
    val id: String,
    val titulo: String?,
    val dataHora: String?,
    val endereco: String?,
    val imagemCapa: String?,
    val gratuito: Boolean?,
    val categoria: CategoriaDto?,
    val fonte: FonteDto?,
    val descricao: String?,
    val linkIngresso: String?,
    val linkSaibaMais: String?,
    val organizador: OrganizadorDto?,
    val urlNoSite: String?
)

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
        @Query("limite") limite: Int? = null
    ): ListaEventosDto

    @GET("eventos/{id}")
    suspend fun evento(@Path("id") id: String): EventoDetalheDto

    @POST("cadastro")
    suspend fun cadastro(@Body body: CadastroBody)

    @GET("eu")
    suspend fun eu()

    @GET("favoritos")
    suspend fun favoritos(): JsonElement

    @POST("favoritos")
    suspend fun favoritar(@Body body: FavoritoBody)

    @DELETE("favoritos/{eventoId}")
    suspend fun desfavoritar(@Path("eventoId") eventoId: String)
}
