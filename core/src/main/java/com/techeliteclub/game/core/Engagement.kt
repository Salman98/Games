package com.techeliteclub.game.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/** Google's in-app "rate this app" popup, after the 5th 3-star win and at most once a month. */
object RateApp {
    private const val MONTH_MS = 30L * 24 * 60 * 60 * 1000

    fun onGreatWin(activity: Activity, store: GameStore) {
        val wins = store.get("great_wins") + 1
        store.put("great_wins", wins)
        val now = System.currentTimeMillis()
        if (wins < 5 || now - store.getLong("rate_asked_at") < MONTH_MS) return
        store.putLong("rate_asked_at", now)
        val manager = ReviewManagerFactory.create(activity)
        // Google decides whether the popup really shows (it limits how often), so failures are fine.
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) manager.launchReviewFlow(activity, task.result)
        }
    }
}

/** Daily evening reminder, skipped on days the player already played. */
object Reminder {
    private const val CHANNEL = "daily_reminder"
    private val MESSAGES = listOf(
        "Your next level is ready" to "A quick puzzle break? Tap to play.",
        "Time for a puzzle?" to "Two minutes, one level. You've got this.",
        "Ready for a new challenge?" to "Your board is waiting for you.",
    )

    /** Local calendar day number, to compare "played today". */
    fun today(): Long {
        val now = System.currentTimeMillis()
        return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
    }

    fun markPlayed(store: GameStore) = store.putLong("last_played_day", today())

    /** Once a day around 19:00 local time. Keeps an existing schedule. */
    fun schedule(context: Context) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(next.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("daily_reminder", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    @SuppressLint("MissingPermission") // checked just below on Android 13+, not needed before
    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT).setName("Daily reminder").build()
        )
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) ?: return
        val open = PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val (title, text) = MESSAGES[(today() % MESSAGES.size).toInt()]
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(context.applicationInfo.loadLabel(context.packageManager))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(1, notification)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val store = GameStore(applicationContext)
        if (store.getBool("reminders", true) && store.getLong("last_played_day", -1L) != Reminder.today()) {
            Reminder.show(applicationContext)
        }
        return Result.success()
    }
}
