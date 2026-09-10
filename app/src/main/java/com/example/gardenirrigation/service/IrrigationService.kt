package com.example.gardenirrigation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gardenirrigation.MainActivity
import com.example.gardenirrigation.model.IrrigationData
import com.example.gardenirrigation.model.IrrigationStage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IrrigationService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    val currentStageIndex = MutableStateFlow(0)
    val currentStageRemainingSeconds = MutableStateFlow(0L)
    val totalElapsedTimeSeconds = MutableStateFlow(0L)
    val isRunning = MutableStateFlow(false)
    val isFinished = MutableStateFlow(false)

    private var ringtone: Ringtone? = null

    inner class LocalBinder : Binder() {
        fun getService(): IrrigationService = this@IrrigationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        resetTimer()
    }

    fun startIrrigation() {
        if (isFinished.value) resetTimer()
        if (!isRunning.value) {
            isRunning.value = true
            startForegroundService()
            runTimer()
        }
    }

    fun pauseIrrigation() {
        isRunning.value = false
        timerJob?.cancel()
        stopAlarm()
    }

    fun resumeIrrigation() {
        if (!isRunning.value && !isFinished.value) {
            isRunning.value = true
            runTimer()
        }
    }

    private fun resetTimer() {
        currentStageIndex.value = 0
        currentStageRemainingSeconds.value = IrrigationData.stages[0].durationSeconds
        totalElapsedTimeSeconds.value = 0L
        isFinished.value = false
        isRunning.value = false
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isRunning.value && !isFinished.value) {
                delay(1000L)
                totalElapsedTimeSeconds.value += 1

                if (currentStageRemainingSeconds.value > 0) {
                    currentStageRemainingSeconds.value -= 1
                } else {
                    playAlarm()
                    if (currentStageIndex.value < IrrigationData.stages.lastIndex) {
                        currentStageIndex.value += 1
                        currentStageRemainingSeconds.value =
                            IrrigationData.stages[currentStageIndex.value].durationSeconds
                    } else {
                        isFinished.value = true
                        isRunning.value = false
                        break
                    }
                }
                updateNotification()
            }
        }
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
            
            serviceScope.launch {
                delay(5000L)
                stopAlarm()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        ringtone?.let {
            if (it.isPlaying) it.stop()
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stageName = if (isFinished.value) "آبیاری تمام شد" else IrrigationData.stages[currentStageIndex.value].name

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مدیریت آبیاری باغ")
            .setContentText("مرحله فعلی: $stageName")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Irrigation Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopAlarm()
    }

    companion object {
        private const val CHANNEL_ID = "irrigation_channel"
        private const val NOTIFICATION_ID = 101
    }
}
