package com.binil.pushnotification;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class DeviceInfo {
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
//            File installation = new File(context.getFilesDir(), INSTALLATION);
//            try {
//                if (!installation.exists()) writeInstallationFile(context, installation);
//                sID = readInstallationFile(installation);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
            sID = getDeviceID(context);
        }
        if (BuildConfig.DEBUG) Log.d("Installation", "ID = " + sID);
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(Context context, File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);

        String id = getDeviceID(context);

        out.write(id.getBytes());
        out.close();
    }

    private static String getDeviceID(Context context) {
        // INFO:
        // http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
        // http://android-developers.blogspot.com/2011/03/identifying-app-installations.html

        // Prefer ANDROID_ID
        String id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (BuildConfig.DEBUG) Log.d("Installation", "Write ID_AND = " + id);

        if (id == null || id.length() == 0 || id.equals("9774d56d682e549c")) {
            // try IMEI
            // note: require
            // <uses-permission android:name="android.permission.READ_PHONE_STATE" android:required="false" />
            try {
                id = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            } catch (Exception ex) {
                ex.printStackTrace();
                id = "";
            }
            if (BuildConfig.DEBUG) Log.d("Installation", "Write ID_IM = " + id);
        }

        if (id == null || id.length() == 0) {
            // try MAC address
            try {
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                id = wm.getConnectionInfo().getMacAddress();
            } catch (Exception ex) {
                ex.printStackTrace();
                id = "";
            }
            if (BuildConfig.DEBUG) Log.d("Installation", "Write ID_MA = " + id);
        }

        if (id == null || id.length() == 0) {
            // use UUID only as last option as reinstallation app in same device
            // will cause it differ
            id = UUID.randomUUID().toString();
            if (BuildConfig.DEBUG) Log.d("Installation", "Write ID_UU = " + id);
        }
        return id;
    }
}
