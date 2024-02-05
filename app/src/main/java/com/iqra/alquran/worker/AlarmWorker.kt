package com.iqra.alquran.worker

import android.annotation.SuppressLint
import com.iqra.alquran.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.iqra.alquran.receivers.NamazTimeUpdateReceiver
import com.iqra.alquran.repository.Repository
import com.iqra.alquran.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmWorker(var context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        fun updateAlarms(context: Context, delay: Boolean) {
            Log.d("TAG_NAMAZ", "update alarm")

            val sharedPreferences = context.getSharedPreferences(
                "Settings",
                Context.MODE_PRIVATE
            )
            val lat = sharedPreferences.getLong(Constants.KEY_LAT, 0)
            val long = sharedPreferences.getLong(Constants.KEY_LONG, 0)
            if (lat > 0 && long > 0) {
                var alarmDelayTime: Long = 0
                if (delay) {
                    val currentTime = Calendar.getInstance()
                    val alarmTime = Calendar.getInstance()
                    alarmTime.set(Calendar.HOUR_OF_DAY, 1)
                    alarmTime.set(Calendar.MINUTE, 0)
                    alarmTime.set(Calendar.SECOND, 0)
                    if (!alarmTime.after(currentTime)) {
                        alarmTime.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    alarmDelayTime = alarmTime.timeInMillis - currentTime.timeInMillis
                }
                val workManager = WorkManager.getInstance(context)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val data = Data.Builder()
                    .putString(Constants.KEY_LAT, lat.toString())
                    .putString(Constants.KEY_LONG, long.toString())
                    .build()
                val request = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
                    .setInitialDelay(alarmDelayTime, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()
                workManager.enqueueUniqueWork("alarm_worker", ExistingWorkPolicy.REPLACE, request)
            }
        }
    }

    override suspend fun doWork(): Result {
        Log.d("TAG_NAMAZ", "do work")

        clearAlarm(1)
        clearAlarm(2)
        clearAlarm(3)
        clearAlarm(4)
        clearAlarm(5)

        val sharedPreferences = context.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )

        val namazAlarmList =
            sharedPreferences.getStringSet(Constants.KEY_NAMAZ_ALARMS, setOf<String>())
                ?.toMutableList()

        if (namazAlarmList == null || namazAlarmList.size == 0)
            return Result.success()

        withContext(Dispatchers.IO) {
            val repository = Repository()
            val namazTimings = repository.getNamazTimings(
                (System.currentTimeMillis() / 1000).toString(),
                inputData.getString(Constants.KEY_LAT)!!,
                inputData.getString(Constants.KEY_LONG)!!
            )
            if (namazTimings.data != null) {
                for (alarm in namazAlarmList) {
                    when (alarm) {
                        Constants.NAMAZ[0] -> {
                            setAlarm(1, context.getString(R.string.fajr), namazTimings.data.data.timings.Fajr)
                        }
                        Constants.NAMAZ[1] -> {
                            setAlarm(2, context.getString(R.string.dhuhr), namazTimings.data.data.timings.Dhuhr)
                        }
                        Constants.NAMAZ[2] -> {
                            setAlarm(3, context.getString(R.string.asr), namazTimings.data.data.timings.Asr)
                        }
                        Constants.NAMAZ[3] -> {
                            setAlarm(4, context.getString(R.string.maghrib), namazTimings.data.data.timings.Maghrib)
                        }
                        else -> {
                            setAlarm(5, context.getString(R.string.isha), namazTimings.data.data.timings.Isha)
                        }
                    }
                }
            }
        }

        updateAlarms(context, true)

        return Result.success()
    }

    fun clearAlarm(requestCode: Int) {
        val alarm = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NamazTimeUpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(requestCode: Int, namaz: String, time: String) {
        Log.d("TAG_NAMAZ", "set alarm")

        val alarm = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NamazTimeUpdateReceiver::class.java)
        intent.setData(Uri.parse(namaz))
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val currentTime = Calendar.getInstance()
        val alarmTime = Calendar.getInstance()
        alarmTime.set(Calendar.HOUR_OF_DAY, time.substring(0, 2).toInt())
        alarmTime.set(Calendar.MINUTE, time.substring(3).toInt())
        alarmTime.set(Calendar.SECOND, 0)
        if (alarmTime.after(currentTime)) {
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                pendingIntent
            )
        }
    }
}