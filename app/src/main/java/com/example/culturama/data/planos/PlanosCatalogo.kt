package com.example.culturama.data.planos

/**
 * Catálogo estático dos planos comerciais do Culturama.
 * Somente apresentação demonstrativa para o TCC — sem cobrança ou backend.
 */
enum class DestaqueDoPlano {
    SIMPLES,
    INTERMEDIARIO,
    PREMIUM
}

data class PlanoApresentacao(
    val id: String,
    val nome: String,
    val categoria: String,
    val preco: String,
    val publico: String,
    val beneficios: List<String>,
    val botao: String,
    val acao: AcaoDoPlano,
    val destaque: DestaqueDoPlano,
    val nota: String? = null
)

enum class AcaoDoPlano {
    CADASTRO_ORGANIZADOR,
    CADASTRO_EMPRESA,
    CONHECER_EMPRESAS,
    EM_BREVE
}

data class LinhaComparacao(
    val criterio: String,
    val valores: List<String>
)

object PlanosCatalogo {

    val organizadores: List<PlanoApresentacao> = listOf(
        PlanoApresentacao(
            id = "livre",
            nome = "Culturama Livre",
            categoria = "Gratuito",
            preco = "R$ 0",
            publico = "Pequenos organizadores, coletivos independentes e quem está começando a divulgar.",
            beneficios = listOf(
                "Publicação de até 3 novos eventos por semana",
                "Divulgação na aba Eventos",
                "Presença no mapa cultural com marcador padrão",
                "Perfil público do organizador",
                "Acesso ao mural de gerenciamento, conforme as funcionalidades disponíveis",
                "Sem destaque patrocinado"
            ),
            botao = "Começar gratuitamente",
            acao = AcaoDoPlano.CADASTRO_ORGANIZADOR,
            destaque = DestaqueDoPlano.SIMPLES
        ),
        PlanoApresentacao(
            id = "plus",
            nome = "Culturama Plus",
            categoria = "Intermediário",
            preco = "Valor mensal a definir",
            publico = "Organizadores que querem ampliar a visibilidade e alcançar mais pessoas.",
            beneficios = listOf(
                "Publicação de até 5 novos eventos por semana",
                "Todos os benefícios do Culturama Livre",
                "Destaque visual dos eventos no mapa",
                "Maior visibilidade durante a exploração cultural",
                "Indicadores básicos de visualizações e favoritos, quando disponíveis",
                "Acesso ao mural de eventos"
            ),
            botao = "Em breve",
            acao = AcaoDoPlano.EM_BREVE,
            destaque = DestaqueDoPlano.INTERMEDIARIO,
            nota = "A contratação deste plano ainda não está disponível."
        ),
        PlanoApresentacao(
            id = "max",
            nome = "Culturama Max",
            categoria = "Premium",
            preco = "Valor mensal a definir",
            publico = "Organizadores e espaços com programação frequente e busca por maior visibilidade.",
            beneficios = listOf(
                "Publicação ilimitada de novos eventos, sujeita às regras da plataforma",
                "Todos os benefícios dos planos anteriores",
                "Destaques rotativos na página inicial (Home)",
                "Destaques no carrossel da aba Eventos",
                "Destaques visuais no mapa cultural",
                "Indicadores de desempenho, quando disponíveis",
                "Possibilidade futura de campanhas promocionais"
            ),
            botao = "Em breve",
            acao = AcaoDoPlano.EM_BREVE,
            destaque = DestaqueDoPlano.PREMIUM,
            nota = "Destaques são rotativos e não garantem visualizações, vendas ou comparecimento. Contratação em breve."
        )
    )

    val empresas: List<PlanoApresentacao> = listOf(
        PlanoApresentacao(
            id = "local",
            nome = "Empresa Local",
            categoria = "Gratuito",
            preco = "R$ 0",
            publico = "Empresas de pequeno porte que desejam conhecer eventos e iniciativas culturais locais.",
            beneficios = listOf(
                "Cadastro empresarial",
                "Exploração de eventos culturais",
                "Consulta de informações públicas",
                "Demonstração do Culturama Insights",
                "Descoberta de oportunidades de patrocínio",
                "Possibilidade futura de demonstrar interesse em eventos"
            ),
            botao = "Cadastrar minha empresa",
            acao = AcaoDoPlano.CADASTRO_EMPRESA,
            destaque = DestaqueDoPlano.SIMPLES
        ),
        PlanoApresentacao(
            id = "conexao",
            nome = "Empresa Conexão",
            categoria = "Médio porte",
            preco = "Comissão percentual a definir sobre vendas de ingressos atribuídas ao Culturama",
            publico = "Marcas que querem acompanhar interesse cultural e conectar-se a organizadores.",
            beneficios = listOf(
                "Indicadores agregados de interesse cultural",
                "Identificação de eventos e categorias relevantes",
                "Acompanhamento de campanhas",
                "Relatórios de conversão, quando disponíveis",
                "Informações sobre regiões com potencial para patrocínio",
                "Oportunidades de conexão com organizadores"
            ),
            botao = "Conhecer benefícios",
            acao = AcaoDoPlano.CONHECER_EMPRESAS,
            destaque = DestaqueDoPlano.INTERMEDIARIO,
            nota = "Cobrança ainda não implementada. Modelo comercial demonstrativo."
        ),
        PlanoApresentacao(
            id = "impacto",
            nome = "Empresa Impacto",
            categoria = "Grande porte",
            preco = "Condições comerciais personalizadas",
            publico = "Empresas que buscam parcerias culturais em escala e Insights avançados.",
            beneficios = listOf(
                "Culturama Insights avançado",
                "Campanhas em diferentes regiões",
                "Oportunidades de patrocínio de múltiplos eventos",
                "Indicadores agregados de desempenho",
                "Relatórios de campanhas",
                "Recursos avançados de relacionamento com organizadores, quando disponíveis"
            ),
            botao = "Conhecer benefícios",
            acao = AcaoDoPlano.CONHECER_EMPRESAS,
            destaque = DestaqueDoPlano.PREMIUM,
            nota = "Possibilidades futuras: comissão negociada, remuneração por público verificável e patrocínios personalizados."
        )
    )

    val comparacaoOrganizadores: List<LinhaComparacao> = listOf(
        LinhaComparacao("Publicações / semana", listOf("Até 3", "Até 5", "Ilimitado*")),
        LinhaComparacao("Destaque no mapa", listOf("Padrão", "Sim", "Sim")),
        LinhaComparacao("Destaque na Home", listOf("Não", "Não", "Rotativo")),
        LinhaComparacao("Destaque na aba Eventos", listOf("Não", "Não", "Rotativo")),
        LinhaComparacao("Indicadores", listOf("—", "Básicos**", "Completos**"))
    )

    val comparacaoEmpresas: List<LinhaComparacao> = listOf(
        LinhaComparacao("Informações públicas", listOf("Sim", "Sim", "Sim")),
        LinhaComparacao("Culturama Insights", listOf("Demo", "Agregado", "Avançado")),
        LinhaComparacao("Campanhas", listOf("—", "Acompanhar", "Multi-região")),
        LinhaComparacao("Relatórios de conversão", listOf("—", "Quando disponíveis", "Quando disponíveis")),
        LinhaComparacao("Patrocínio", listOf("Descoberta", "Conexão", "Personalizado"))
    )
}
