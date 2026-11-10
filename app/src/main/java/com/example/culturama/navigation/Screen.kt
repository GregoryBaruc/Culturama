package com.example.culturama.navigation

import android.net.Uri

/**
 * Rotas de navegação do aplicativo Culturama.
 * Centralizadas para manter uma arquitetura escalável e desacoplada.
 */
sealed class Screen(val route: String) {
    object Home            : Screen("home")
    object Events          : Screen("events?mapa={mapa}") {
        fun createRoute(mapa: Boolean = false): String = "events?mapa=$mapa"
    }
    object EventDetail     : Screen("eventDetail?eventId={eventId}")
    object Access          : Screen("access")
    object Preferences     : Screen("preferences")
    object Organizers      : Screen("organizers")
    object ParaEmpresas    : Screen("para_empresas")
    object OrganizerAccess : Screen("organizer_access")
    object Login           : Screen("login") {
        fun createRoute(registeredEmail: String? = null, destino: String? = null): String {
            val params = mutableListOf<String>()
            if (!registeredEmail.isNullOrBlank()) params += "email=${Uri.encode(registeredEmail)}"
            if (!destino.isNullOrBlank() && destino != "descoberta") {
                params += "destino=${Uri.encode(destino)}"
            }
            return if (params.isEmpty()) "login" else "login?${params.joinToString("&")}"
        }
    }
    object Acessos         : Screen("acessos") {
        fun createRoute(foco: String = ""): String =
            if (foco.isBlank()) "acessos" else "acessos?foco=${Uri.encode(foco)}"
    }
    object AreaDaConta     : Screen("area/{tipo}") {
        fun createRoute(tipo: String): String = "area/${Uri.encode(tipo)}"
    }
    object SolicitacaoDeAcesso : Screen("solicitacao/{tipo}") {
        fun createRoute(tipo: String): String = "solicitacao/${Uri.encode(tipo)}"
    }
    object SignUp          : Screen("signup")
    object CompleteProfile : Screen("complete_profile")
    object Profile         : Screen("profile")
    object Settings        : Screen("settings")
    object UserSection     : Screen("user_section/{section}") {
        fun createRoute(section: String): String = "user_section/$section"
    }
    object AdminPainel     : Screen("admin/{secao}") {
        fun createRoute(secao: SecaoDoPainel): String = "admin/${secao.id}"
    }
    object Moderacao       : Screen("moderacao")
}

/** Seções do painel. Só a visão geral está implementada. */
enum class SecaoDoPainel(val id: String, val titulo: String) {
    VISAO("visao", "Visão geral"),
    EVENTOS("eventos", "Eventos"),
    ANUNCIOS("anuncios", "Anúncios"),
    EMPRESAS("empresas", "Empresas");

    companion object {
        fun porId(id: String): SecaoDoPainel? = entries.firstOrNull { it.id == id }
    }
}

/** Seções do menu do usuário que ainda não têm tela própria. */
enum class SecaoDoUsuario(
    val id: String,
    val titulo: String,
    val resumo: String,
    val detalhe: String
) {
    FAVORITOS(
        id = "favoritos",
        titulo = "Meus favoritos",
        resumo = "Seus eventos salvos aparecem aqui",
        detalhe = "A lista completa dos eventos que você salvou entra numa próxima etapa."
    ),
    INTERESSES(
        id = "interesses",
        titulo = "Meus interesses",
        resumo = "Samba, rap, capoeira, dança e outras cenas",
        detalhe = "A edição de categorias e subcategorias culturais entra numa próxima etapa."
    ),
    COMENTARIOS(
        id = "comentarios",
        titulo = "Meus comentários",
        resumo = "Textos que você publicou nos eventos",
        detalhe = "O histórico dos seus comentários entra numa próxima etapa. Notas numéricas ficam em Minhas avaliações."
    ),
    AVALIACOES(
        id = "avaliacoes",
        titulo = "Minhas avaliações",
        resumo = "Notas que você deu aos eventos",
        detalhe = "As notas numéricas entram numa próxima etapa. Comentários em texto ficam em Meus comentários."
    ),
    EVENTOS(
        id = "eventos",
        titulo = "Meus eventos",
        resumo = "Eventos que você cria ou gerencia",
        detalhe = "A criação e o gerenciamento dos seus eventos entram numa próxima etapa, conforme a permissão da conta."
    ),
    TRAJETORIA(
        id = "trajetoria",
        titulo = "Minha trajetória cultural",
        resumo = "Eventos em que você participou",
        detalhe = "O histórico de participação entra numa próxima etapa. Ver ou favoritar um evento não conta como presença."
    ),
    ORGANIZACOES(
        id = "organizacoes",
        titulo = "Organizações e comunidades",
        resumo = "Coletivos e espaços que você acompanha",
        detalhe = "A área de organizações, coletivos e estabelecimentos entra numa próxima etapa."
    ),
    EDITAR_PERFIL(
        id = "editar_perfil",
        titulo = "Editar perfil",
        resumo = "Foto, nome e apresentação",
        detalhe = "A edição de foto, nome, usuário e apresentação entra numa próxima etapa."
    );

    companion object {
        fun porId(id: String): SecaoDoUsuario? = entries.firstOrNull { it.id == id }
    }
}
