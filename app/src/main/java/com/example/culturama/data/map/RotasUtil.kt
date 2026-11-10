package com.example.culturama.data.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Abre um app de mapas. O Culturama não calcula a rota. */
fun abrirRota(context: Context, latitude: Double, longitude: Double, nome: String) {
    val rotulo = Uri.encode(nome.ifBlank { "Evento" })
    val geo = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($rotulo)")
    val geoIntent = Intent(Intent.ACTION_VIEW, geo).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (geoIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(geoIntent, "Como chegar"))
        return
    }
    val web = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
    val webIntent = Intent(Intent.ACTION_VIEW, web).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (webIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(webIntent)
        return
    }
    Toast.makeText(context, "Nenhum app de mapas encontrado.", Toast.LENGTH_SHORT).show()
}
