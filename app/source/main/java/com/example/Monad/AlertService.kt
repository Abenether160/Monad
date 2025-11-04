package com.example.burglaralert

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class AlertService : Service() {

    private val udpPort = 50505
    private val sharedSecret = "CHANGE_THIS_SHARED_SECRET" // keep same on all devices
    private val deviceId = UUID.randomUUID().toString()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        startUdpListener()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "AlertServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alert Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Burglar Alert Service")
            .setContentText("Listening for alerts over Wi-Fi")
            .setSmallIcon(R.drawable.ic_alert)
            .build()
        startForeground(1, notification)
    }

    private fun startUdpListener() {
        scope.launch {
            val socket = DatagramSocket(udpPort)
            socket.broadcast = true
            val buffer = ByteArray(2048)
            while (isActive) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val msg = String(packet.data, 0, packet.length)
                    // Basic validation: sharedSecret must be contained
                    if (msg.contains(sharedSecret)) {
                        triggerAlert(packet.address.hostAddress)
                    }
                } catch (e: Exception) {
                    // ignore, continue listening
                }
            }
            socket.close()
        }
    }

    private fun triggerAlert(senderIp: String) {
        // Vibrate
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        else vibrator.vibrate(500)

        // Notification
        val notifIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, "AlertServiceChannel")
            .setContentTitle("⚠ Burglar Alert")
            .setContentText("Alert from $senderIp")
            .setSmallIcon(R.drawable.ic_alert)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notif)

        // Overlay
        val overlay = AlertOverlay(this)
        overlay.showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra("SEND_ALERT", false) == true) sendAlert()
        return START_STICKY
    }

    private fun sendAlert() {
        scope.launch {
            val socket = DatagramSocket()
            val msg = "$sharedSecret ALERT from $deviceId"
            val data = msg.toByteArray()
            val packet = DatagramPacket(data, data.size, InetAddress.getByName("255.255.255.255"), udpPort)
            socket.broadcast = true
            repeat(3) { socket.send(packet); delay(100) }
            socket.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}