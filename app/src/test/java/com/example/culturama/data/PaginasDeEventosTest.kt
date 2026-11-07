package com.example.culturama.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PaginasDeEventosTest {

    /** Simula a API: `total` itens, `limite` por página. */
    private fun api(total: Int, limite: Int, pedidas: MutableList<Int> = mutableListOf()): suspend (Int) -> Pagina<Int> = { numero ->
        pedidas += numero
        val inicio = (numero - 1) * limite
        val itens = (inicio until minOf(inicio + limite, total)).toList()
        Pagina(itens, numero * limite < total)
    }

    @Test
    fun trazOsEventosDeTodasAsPaginas() = runBlocking {
        val pedidas = mutableListOf<Int>()
        val todos = buscarTodasAsPaginas(buscarPagina = api(191, 100, pedidas))
        assertEquals((0 until 191).toList(), todos)
        assertEquals(listOf(1, 2), pedidas)
    }

    @Test
    fun totalExatoNaoPedePaginaAMais() = runBlocking {
        val pedidas = mutableListOf<Int>()
        assertEquals(200, buscarTodasAsPaginas(buscarPagina = api(200, 100, pedidas)).size)
        assertEquals(listOf(1, 2), pedidas)
    }

    @Test
    fun semEventosDevolveListaVazia() = runBlocking {
        assertEquals(emptyList<Int>(), buscarTodasAsPaginas(buscarPagina = api(0, 100)))
    }

    @Test
    fun servidorQueSempreDizTerMaisNaoPrendeOApp() = runBlocking {
        val todos = buscarTodasAsPaginas(maxPaginas = 3) { Pagina(listOf(it), true) }
        assertEquals(listOf(1, 2, 3), todos)
    }

    @Test
    fun paginaVaziaParaMesmoComTemMais() = runBlocking {
        val pedidas = mutableListOf<Int>()
        buscarTodasAsPaginas<Int> { pedidas += it; Pagina(emptyList(), true) }
        assertEquals(listOf(1), pedidas)
    }

    @Test
    fun falhaNaSegundaPaginaNaoViraListaIncompleta() {
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                buscarTodasAsPaginas<Int> { numero ->
                    if (numero == 2) throw IllegalStateException("sem rede")
                    Pagina(listOf(numero), true)
                }
            }
        }
    }
}
