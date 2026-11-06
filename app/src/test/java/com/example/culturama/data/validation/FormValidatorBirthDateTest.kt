package com.example.culturama.data.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormValidatorBirthDateTest {

    private fun digitosHaAnos(anos: Int, ajusteDias: Int = 0): String {
        val data = Calendar.getInstance().apply {
            add(Calendar.YEAR, -anos)
            add(Calendar.DAY_OF_YEAR, ajusteDias)
        }
        return SimpleDateFormat("ddMMyyyy", Locale.US).format(data.time)
    }

    @Test
    fun `converte para o formato da API`() {
        assertEquals("1999-05-10", FormValidator.birthDateToIso("10051999"))
    }

    @Test
    fun `recusa data incompleta ou que nao existe`() {
        assertFalse(FormValidator.validateBirthDate("1005").isValid)
        assertFalse(FormValidator.validateBirthDate("31022000").isValid)
        assertNull(FormValidator.birthDateToIso("31022000"))
    }

    @Test
    fun `aceita maior de 18 e recusa menor`() {
        assertTrue(FormValidator.validateBirthDate(digitosHaAnos(18, ajusteDias = -1)).isValid)
        assertFalse(FormValidator.validateBirthDate(digitosHaAnos(18, ajusteDias = 1)).isValid)
    }
}
