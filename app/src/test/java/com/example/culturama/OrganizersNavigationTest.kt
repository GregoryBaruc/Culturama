package com.example.culturama

import com.example.culturama.navigation.Screen
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes unitários para validar as novas rotas de organizadores e integridade de navegação.
 */
class OrganizersNavigationTest {

    @Test
    fun organizersRoute_isConfiguredCorrectly() {
        assertEquals("organizers", Screen.Organizers.route)
    }

    @Test
    fun organizerAccessRoute_isConfiguredCorrectly() {
        assertEquals("organizer_access", Screen.OrganizerAccess.route)
    }

    @Test
    fun routes_areUniqueAndNotColliding() {
        val routes = listOf(
            Screen.Home.route,
            Screen.Access.route,
            Screen.Preferences.route,
            Screen.Organizers.route,
            Screen.OrganizerAccess.route,
            Screen.Login.route,
            Screen.SignUp.route
        )
        val distinctRoutes = routes.distinct()
        assertEquals("Todas as rotas do app devem ser únicas", routes.size, distinctRoutes.size)
    }

    @Test
    fun organizerAccessRoute_isDistinctFromCommonUserAuth() {
        assertNotEquals(Screen.OrganizerAccess.route, Screen.Login.route)
        assertNotEquals(Screen.OrganizerAccess.route, Screen.SignUp.route)
        assertNotEquals(Screen.OrganizerAccess.route, Screen.Access.route)
    }
}
