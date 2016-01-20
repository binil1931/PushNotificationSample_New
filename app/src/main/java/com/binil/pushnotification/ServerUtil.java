/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.binil.pushnotification;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtil {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
    private static final String TAG = "GCM";

    /**
     * Registration this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    public static boolean register(final Context context, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        Log.e(TAG, "registering device (regId = " + regId + ")");

/*        String serverUrl = URLConst.registerDevice();
        HashMap<String, Object> params = new HashMap<>();
        params.put("DeviceType", 2);
        params.put("RegistrationId", regId);
        params.put("DeviceId", DeviceInfo.id(context)); // http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
        params.put("GuiVersion", getVersion(context));*/

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {

                //post(serverUrl, params);
                if (!TextUtils.isEmpty(regId)) {
                    Log.i(TAG, "[AgmoStudioSDK] Push server registration success! Device Token => " + regId);
                }
                return true;
            } catch (Exception e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i, e);
                e.printStackTrace();
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        return false;
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @throws java.io.IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, Object> params)
            throws IOException, JSONException {

        URL url = new URL(endpoint);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Cache-Control", "no-cache");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        urlConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
        urlConnection.setRequestProperty("Accept", "*/*");

        urlConnection.setDoOutput(true);

        JSONObject json = new JSONObject(params);
        String body = json.toString();
        urlConnection.setFixedLengthStreamingMode(body.length());

        try {
            OutputStream os = urlConnection.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            int status = urlConnection.getResponseCode();
            String connectionMsg = urlConnection.getResponseMessage();
            urlConnection.disconnect();

            if (status != HttpURLConnection.HTTP_OK) {
                Log.wtf(TAG, connectionMsg);
                throw new IOException("Post failed with error code " + status);
            }
        }

    }

    public static int getVersion(Context context) {
        int version = -1;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            version = pInfo.versionCode;

        } catch (NameNotFoundException e1) {
            Log.e(TAG, "Package Name not found", e1);
        }

        return version;
    }

}
