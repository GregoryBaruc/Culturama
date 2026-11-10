package com.example.culturama.data.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Uma leitura pontual da posição. Não guarda histórico nem pede localização em segundo plano.
 */
object LocalizacaoAtual {

    fun servicosLigados(context: Context): Boolean {
        val gerente = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            gerente.isLocationEnabled
        } else {
            gerente.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                gerente.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun obter(context: Context): Coordenada? = suspendCancellableCoroutine { cont ->
        val cliente = LocationServices.getFusedLocationProviderClient(context)
        val cancelamento = CancellationTokenSource()
        cont.invokeOnCancellation { cancelamento.cancel() }
        cliente.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancelamento.token)
            .addOnSuccessListener { local ->
                if (local == null) cont.resume(null)
                else cont.resume(Coordenada(local.latitude, local.longitude))
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
