package com.example.practica15

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Nuevo token generado: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Mensaje de notificación tradicional
        remoteMessage.notification?.let { notification ->
            Log.d("FCM_MENSAJE", "Cuerpo de la notificación: ${notification.body}")
            mostrarNotificacion(this, "canal_practica_15", notification.title ?: "Alerta", notification.body ?: "")
        }

        // 2. Mensaje con datos ocultos (Data Payload)
        if (remoteMessage.data.isNotEmpty()) {
            val tituloEspecial = remoteMessage.data["titulo_oculto"] ?: "Mensaje Oculto"
            val mensajeEspecial = remoteMessage.data["mensaje_oculto"] ?: "Tienes datos nuevos"
            mostrarNotificacion(this, "canal_practica_15", tituloEspecial, mensajeEspecial)
        }
    }
}
