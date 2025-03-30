package com.mario.skyeye.alarm.workmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mario.skyeye.alarm.AlarmService
import com.mario.skyeye.data.repo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class WeatherAlertWorker (context: Context, workerParams: WorkerParameters, private val repo: Repo): CoroutineWorker(context, workerParams) {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        repeat(10) {
            delay(1000)
            Log.i("WeatherAlertWorker", "Checking weather condition...")
        }
        return withContext(Dispatchers.IO) {
            try {
                val lat = inputData.getDouble("LATITUDE", 0.0)
                val lon = inputData.getDouble("LONGITUDE", 0.0)
                val unit = inputData.getString("UNIT") ?: "metric"
                val condition = inputData.getString("CONDITION") ?: "Rain"
                Log.i("WeatherAlertWorker", "Checking weather condition for $condition at ($lat, $lon)")
                val shouldTriggerAlarm = checkWeatherCondition(lat, lon, unit, condition)

                if (shouldTriggerAlarm) {
                    val intent = Intent(applicationContext, AlarmService::class.java)
                    applicationContext.startForegroundService(intent)
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private suspend fun checkWeatherCondition(lat: Double, lon: Double, unit: String, condition: String): Boolean {
        val weather = repo.getCurrentWeather(lat, lon, unit)?.firstOrNull()
        return (weather?.weather?.get(0)?.description == condition)
    }
}
class WeatherWorkerFactory(private val repo: Repo) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WeatherAlertWorker::class.java.name -> WeatherAlertWorker(appContext, workerParameters, repo)
            else -> null
        }
    }
}
fun scheduleWeatherAlerts(context: Context, lat: Double, lon: Double, unit: String, condition: String) {
    val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
        20, java.util.concurrent.TimeUnit.MINUTES // Adjust interval as needed
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Requires internet
                .build()
        ).setInputData(
            workDataOf(
                "LATITUDE" to lat,
                "LONGITUDE" to lon,
                "UNIT" to unit,
                "CONDITION" to condition
            )
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WeatherAlertWorker",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
    Log.i("WeatherAlertWorker", "Scheduled weather alert worker for $condition at ($lat, $lon)")
}
fun stopWeatherAlarm(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork("WeatherAlertWorker")
    Log.i("WeatherAlertWorker", "Stopped weather alert worker")
    val intent = Intent(context, AlarmService::class.java)
    intent.action = "STOP_ALARM" // Send action to stop the service
    context.startService(intent) // This will trigger stopSelf() inside the service
}