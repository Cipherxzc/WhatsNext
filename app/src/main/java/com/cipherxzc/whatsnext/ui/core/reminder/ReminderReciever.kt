package com.cipherxzc.whatsnext.ui.core.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.cipherxzc.whatsnext.R
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import java.util.concurrent.TimeUnit

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "待办事项提醒"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道
        val channel = NotificationChannel(
            "todo_channel",
            "Todo提醒",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "todo_channel")
            .setContentTitle("任务提醒")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleReminder(context: Context, todo: TodoItemInfo) {
    val dueDate = todo.dueDate ?: return
    val triggerTime = dueDate.time - TimeUnit.MINUTES.toMillis(10)

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", todo.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        todo.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerTime,
        pendingIntent
    )
}

fun cancelReminder(context: Context, todo: TodoItemInfo) {
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        todo.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}