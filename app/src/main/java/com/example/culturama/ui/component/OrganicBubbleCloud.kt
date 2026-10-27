package com.example.culturama.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.culturama.model.CategoryPreference

/**
 * Nuvem orgânica de bolinhas adaptada para smartphones Android.
 *
 * Reproduz o agrupamento visual da imagem de referência:
 * - Linhas intercaladas com bolinhas de tamanhos variados (grandes, médias e pequenas).
 * - Pequenos deslocamentos horizontais para criar a sensação de "nuvem orgânica".
 * - Distribuição equilibrada com destaque para Música, Feiras e Cultura Popular.
 * - Espaçamento seguro que evita sobreposição acidental e garante toques confortáveis.
 */
@Composable
fun OrganicBubbleCloud(
    categories: List<CategoryPreference>,
    selectedIds: Set<String>,
    onToggleCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryMap = categories.associateBy { it.id }

    // Agrupamento orgânico em fileiras interligadas
    val rows = listOf(
        // Linha 1: Topo — Cinema, Literatura, Outros
        listOfNotNull(
            categoryMap["cinema"],
            categoryMap["literatura"],
            categoryMap["outros"]
        ),
        // Linha 2: Superior Central — Geek e games, Artes Cênicas, Cultura Popular (Destaque L)
        listOfNotNull(
            categoryMap["geek_games"],
            categoryMap["artes_cenicas"],
            categoryMap["cultura_popular"]
        ),
        // Linha 3: Centro Superior — Feiras e gastronomia (Destaque L), Oficinas e cursos
        listOfNotNull(
            categoryMap["feiras_gastronomia"],
            categoryMap["oficinas"]
        ),
        // Linha 4: Centro Principal — Música (Destaque L), Dança, Arte e exposição
        listOfNotNull(
            categoryMap["musica"],
            categoryMap["danca"],
            categoryMap["arte_exposicao"]
        ),
        // Linha 5: Centro Inferior — Esportes, Samba, Batalha de Rima
        listOfNotNull(
            categoryMap["esportes"],
            categoryMap["samba"],
            categoryMap["batalha_rima"]
        ),
        // Linha 6: Base Cultural — Capoeira, Sarau e poesia
        listOfNotNull(
            categoryMap["capoeira"],
            categoryMap["sarau"]
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-14).dp) // Leve aproximação para aspecto de nuvem
    ) {
        rows.forEachIndexed { rowIndex, rowCategories ->
            // Leve deslocamento horizontal alternado para efeito orgânico
            val horizontalOffset = when (rowIndex % 3) {
                0 -> 0.dp
                1 -> 8.dp
                else -> (-8).dp
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = horizontalOffset),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowCategories.forEach { category ->
                    CategoryBubble(
                        category = category,
                        isSelected = category.id in selectedIds,
                        onToggle = { onToggleCategory(category.id) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
