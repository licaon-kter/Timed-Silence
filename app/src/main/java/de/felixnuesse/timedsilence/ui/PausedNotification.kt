package de.felixnuesse.timedsilence.ui

/**
 * Copyright (C) 2019  Felix Nüsse
 * Created on 29.04.19 - 00:13
 * <p>
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 * <p>
 * <p>
 * This program is released under the GPLv3 license
 * <p>
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.R
import de.felixnuesse.timedsilence.Constants.Companion.APP_NAME
import de.felixnuesse.timedsilence.MainActivity
import de.felixnuesse.timedsilence.PrefConstants
import de.felixnuesse.timedsilence.handler.SharedPreferencesHandler
import de.felixnuesse.timedsilence.handler.volume.AlarmHandler
import de.felixnuesse.timedsilence.services.PauseTimerService
import de.felixnuesse.timedsilence.services.`interface`.TimerInterface

class PausedNotification : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(APP_NAME, "PausedNotification: Recieved Intent!")
        if(intent?.action== ACTION_END_PAUSE){
            context?.let {
                AlarmHandler.createRepeatingTimecheck(it)
                AlarmHandler.checkIfNextAlarmExists(it)
            }
        }
    }

    companion object {

        private const val ACTION_END_PAUSE = "ACTION_END_PAUSE"
        private const val NOTIFICATION_ID = 498

        fun show(context: Context){
            Log.e(APP_NAME, "PausedNotification: Show Notification")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(SharedPreferencesHandler.getPref(context, PrefConstants.PREF_PAUSE_NOTIFICATION, PrefConstants.PREF_PAUSE_NOTIFICATION_DEFAULT)){
                notificationManager.notify(NOTIFICATION_ID, buildNotification(context))
            }
        }

        fun buildNotification(context: Context): Notification {

            val cid = "PausedNotification"
            val cname = context.getString(R.string.PausedNotification)

            //NotificationManager.IMPORTANCE_NONE does not update
            val chan = NotificationChannel(cid, cname, NotificationManager.IMPORTANCE_LOW)

            chan.lockscreenVisibility = Notification.VISIBILITY_SECRET

            val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)

            val snoozeIntent = Intent(context, PausedNotification::class.java).apply {
                action = ACTION_END_PAUSE
            }
            val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)

            return Notification.Builder(context, cid)
                .setContentTitle(context.getString(R.string.PausedNotification_TITLE))
                .setContentText(context.getString(R.string.PausedNotification_CONTENT))
                .setSmallIcon(R.drawable.logo_pause)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(0,context.getString(R.string.PausedNotification_RESUME), snoozePendingIntent)
                .build()
        }

        fun cancelNotification(context: Context) {
            Log.e(APP_NAME, "PausedNotification: Cancel Notification")
            var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }

    }

}