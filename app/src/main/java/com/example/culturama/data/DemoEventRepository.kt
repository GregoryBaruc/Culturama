package com.example.culturama.data

import com.example.culturama.model.EventModel

/**
 * Fonte local de dados demonstrativos.
 * Centralizada aqui para facilitar a substituição futura por uma API REST.
 * TODO: Substituir por chamadas de rede (Retrofit/Ktor) na próxima etapa.
 */
object DemoEventRepository {

    fun getWeekEvents(): List<EventModel> = listOf(
        EventModel(
            id = "evt_001",
            title = "Batalha de Rima — Zona Leste",
            description = "A maior batalha de rima da Zona Leste. MCs de todo o estado se encontram para uma competição épica de freestyle. Evento aberto ao público, traga sua torcida!",
            category = "Hip-Hop",
            categoryColor = 0xFFE8835A,
            dayOfMonth = 28,
            monthAbbr = "OUT",
            dayOfWeek = "Qui",
            time = "20h",
            venueName = "Arena Cultural Leste",
            address = "Local demonstrativo — Zona Leste",
            isFree = true,
            organizerName = "Coletivo Hip-Hop SP"
        ),
        EventModel(
            id = "evt_002",
            title = "Roda de Samba do Bairro",
            description = "Roda de samba raiz com músicos locais. Uma tarde de muita alegria, tradição e cultura popular. Ambiente familiar e acolhedor.",
            category = "Samba",
            categoryColor = 0xFFE8835A,
            dayOfMonth = 29,
            monthAbbr = "OUT",
            dayOfWeek = "Sex",
            time = "15h",
            venueName = "Sede do Samba Carioca",
            address = "Local demonstrativo — Centro",
            isFree = true,
            organizerName = "Grupo Samba Raiz"
        ),
        EventModel(
            id = "evt_003",
            title = "Roda de Capoeira Aberta",
            description = "Roda de capoeira aberta à participação de todos os níveis. Mestre reconhecido conduz a roda com muito axé e tradição afro-brasileira.",
            category = "Capoeira",
            categoryColor = 0xFF4A90D9,
            dayOfMonth = 30,
            monthAbbr = "OUT",
            dayOfWeek = "Sáb",
            time = "10h",
            venueName = "Praça da Liberdade",
            address = "Local demonstrativo — Praça pública",
            isFree = true,
            organizerName = "Grupo de Capoeira Angola"
        ),
        EventModel(
            id = "evt_004",
            title = "Sarau de Poesia Periférica",
            description = "Sarau literário com poetas da periferia. Noite de poesia, contos e música. Inscrições para novos participantes abertas na entrada.",
            category = "Sarau",
            categoryColor = 0xFF7B68EE,
            dayOfMonth = 31,
            monthAbbr = "OUT",
            dayOfWeek = "Sáb",
            time = "19h",
            venueName = "Biblioteca Comunitária",
            address = "Local demonstrativo — Bairro periférico",
            isFree = false,
            price = "R$ 10,00",
            organizerName = "Sarau das Letras"
        ),
        EventModel(
            id = "evt_005",
            title = "Encontro de Hip-Hop",
            description = "Evento que reúne grafiteiros, DJs, MCs e dançarinos de break. Quatro elementos do hip-hop em um único espaço cultural.",
            category = "Hip-Hop",
            categoryColor = 0xFFE8835A,
            dayOfMonth = 31,
            monthAbbr = "OUT",
            dayOfWeek = "Sáb",
            time = "14h",
            venueName = "Centro Cultural da Periferia",
            address = "Local demonstrativo — Zona Norte",
            isFree = true,
            organizerName = "Movimento Hip-Hop Unido"
        ),
        EventModel(
            id = "evt_006",
            title = "Feira Cultural Independente",
            description = "Feira de arte, artesanato, gastronomia e cultura independente. Mais de 50 expositores locais. Música ao vivo durante todo o dia.",
            category = "Feiras e gastronomia",
            categoryColor = 0xFF4CAF50,
            dayOfMonth = 1,
            monthAbbr = "NOV",
            dayOfWeek = "Dom",
            time = "10h",
            venueName = "Parque Municipal",
            address = "Local demonstrativo — Parque público",
            isFree = false,
            price = "R$ 5,00",
            organizerName = "Coletivo Arte Livre"
        ),
        EventModel(
            id = "evt_007",
            title = "Festival de Música Local",
            description = "Festival com bandas e artistas locais independentes. Rock, MPB, jazz e soul em um só lugar. Noite especial de descoberta musical.",
            category = "Música",
            categoryColor = 0xFFE8835A,
            dayOfMonth = 1,
            monthAbbr = "NOV",
            dayOfWeek = "Dom",
            time = "18h",
            venueName = "Espaço Cultural Demo",
            address = "Local demonstrativo — Zona Sul",
            isFree = false,
            price = "R$ 25,00",
            organizerName = "Produtora Musical SP"
        )
    )

    fun getEventById(id: String): EventModel? =
        getWeekEvents().find { it.id == id }
}
