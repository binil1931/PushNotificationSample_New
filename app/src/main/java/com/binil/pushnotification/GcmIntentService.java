/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.binil.pushnotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends GcmListenerService {
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "GCM";

    @Override
    public void onMessageReceived(String from, Bundle extras) {

        Log.i(TAG, "from = " + from);
        for (String key : extras.keySet()) {
            Log.i(TAG, "key = " + key + ", value = " + extras.get(key));
        }
        sendNotification(extras);
        Log.i(TAG, "Received: " + extras.toString());
    }

    private void sendNotification(Bundle extras) {
        String msg = extras.getString("Title");

        Intent intent = generateIntent(extras);
        if (intent != null) {
            PendingIntent pi;
            int id = (int) (Math.random() * 100000);
            pi = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            displayNotification(pi, getString(R.string.app_name), msg);
        }
    }

    private Intent generateIntent(Bundle extras) {

        Intent intent = null;

        try {
            String relatedId = extras.getString("AgmoActionContent", "");
            switch (Integer.parseInt(extras.getString("AgmoActionType", "0"))) {
//                case Constants.NotificationType.MESSAGE:
//                case Constants.NotificationType.URL:
//                case Constants.NotificationType.REQUEST_NEW:
//                case Constants.NotificationType.REQUEST_ASSIGNED:
//                case Constants.NotificationType.REQUEST_USER_CANCEL:
//                case Constants.NotificationType.REQUEST_DOCTOR_CANCEL:
//                case Constants.NotificationType.REQUEST_COMPLETE:
                default:
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("forNotification", "yes");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtras(extras);
                    break;
            }
        } catch (Exception ex) {
            intent = new Intent(this, MainActivity.class);
        }

        return intent;
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void displayNotification(PendingIntent pi, String title, String msg) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(((BitmapDrawable) ContextCompat.getDrawable(getApplication(), R.drawable.ic_launcher)).getBitmap())
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setContentIntent(pi)
                        .setTicker(msg)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
