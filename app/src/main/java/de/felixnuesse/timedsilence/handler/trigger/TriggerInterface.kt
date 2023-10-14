package de.felixnuesse.timedsilence.handler.trigger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.R
import de.felixnuesse.timedsilence.ui.notifications.PausedNotification
import java.text.DateFormat
import java.util.*

interface TriggerInterface {

    var mContext: Context

    fun createTimecheck()

    fun removeTimecheck()

    fun createBroadcast(): PendingIntent?

    fun createBroadcast(flag: Int): PendingIntent?

    fun checkIfNextAlarmExists(): Boolean {
        val pIntent = createBroadcast(PendingIntent.FLAG_NO_CREATE)
        return if (pIntent == null) {
            Log.d(Constants.APP_NAME, "TriggerInterface: There is no next Alarm set!")
            PausedNotification.show(mContext)
            false
        } else {
            Log.d(Constants.APP_NAME, "TriggerInterface: There is an upcoming Alarm!")
            PausedNotification.cancelNotification(mContext)
            true
        }
    }

    fun getNextAlarmTimestamp(): String {
        val alarms = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val clockInfo = alarms.nextAlarmClock ?: return mContext.getString(R.string.no_next_time_set)

        Log.d(Constants.APP_NAME, "TriggerInterface: Next Runtime: " + clockInfo.triggerTime)
        return DateFormat.getDateInstance().format(Date(clockInfo.triggerTime))

    }
}