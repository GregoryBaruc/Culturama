package com.example.culturama.navigation

/**
 * Rotas de navegação do aplicativo Culturama.
 * Centralizadas para manter uma arquitetura escalável e desacoplada.
 */
sealed class Screen(val route: String) {
    object Home            : Screen("home")
    object Events          : Screen("events")
    object EventDetail     : Screen("eventDetail?eventId={eventId}")
    object Access          : Screen("access")
    object Preferences     : Screen("preferences")
    object Organizers      : Screen("organizers")
    object OrganizerAccess : Screen("organizer_access")
    object Login           : Screen("login") {
        fun createRoute(registeredEmail: String? = null): String {
            return if (!registeredEmail.isNullOrBlank()) {
                "login?email=$registeredEmail"
            } else {
                "login"
            }
        }
    }
    object SignUp          : Screen("signup")
    object CompleteProfile : Screen("complete_profile")
}
