package com.pulsecharge.app.alerts

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pulsecharge.app.MainActivity
import com.pulsecharge.app.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "pulse_charge_alerts"
        const val CHANNEL_NAME = "Battery & Health Alerts"
        const val NOTIF_ID_TEST = 1001
        const val NOTIF_ID_FULL_CHARGE = 1002
        const val NOTIF_ID_LOW_BATTERY = 1003
        const val NOTIF_ID_OVERHEAT = 1004
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Configurable alerts for full charge, low battery, and thermal limits."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showTestNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("PulseCharge Alert System Ready")
            .setContentText("Test notification successful! Your custom battery threshold alerts are active.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showFullChargeNotification(level: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Battery Charged ($level%)")
            .setContentText("Your device has reached your target charge limit of $level%. Unplug to prolong battery health.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIF_ID_FULL_CHARGE, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showLowBatteryNotification(level: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Low Battery Warning ($level%)")
            .setContentText("Battery level dropped to $level%. Plug in your charger to prevent shutdown.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIF_ID_LOW_BATTERY, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showOverheatNotification(tempC: Float) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Overheat Warning (${tempC.toInt()}°C)")
            .setContentText("Battery temperature reached ${tempC.toInt()}°C! Stop heavy tasks or remove case to cool down.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIF_ID_OVERHEAT, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun showDailyDigestNotification(peakTempC: Float) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("PulseCharge Daily Digest")
            .setContentText("Today: Battery overall health Good. Peak temp ${peakTempC.toInt()}°C. 1 full cycle completed.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1005, builder.build())
    }
}
