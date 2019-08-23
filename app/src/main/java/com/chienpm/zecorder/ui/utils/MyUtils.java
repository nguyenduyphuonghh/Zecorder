package com.chienpm.zecorder.ui.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chienpm.zecorder.ui.services.sync.SyncService;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MyUtils {
    public static final int RESULT_CODE_FAILED = -999999;
    public static final String SCREEN_CAPTURE_INTENT_RESULT_CODE = "SCREEN_CAPTURE_INTENT_RESULT_CODE";
    public static final String ACTION_OPEN_SETTING_ACTIVITY = "ACTION_OPEN_SETTING_ACTIVITY";
    public static final String ACTION_OPEN_LIVE_ACTIVITY = "ACTION_OPEN_LIVE_ACTIVITY";
    public static final String ACTION_OPEN_VIDEO_MANAGER_ACTIVITY = "ACTION_OPEN_VIDOE_MANAGER_ACTIVITY";
    public static final String ACTION_UPDATE_SETTING = "ACTION_UPDATE_SETTING";
    public static final int SELECTED_MODE_EMPTY = 0;
    public static final int SELECTED_MODE_ALL = 1;
    public static final int SELECTED_MODE_MULTIPLE = 2;
    public static final int SELECTED_MODE_SINGLE = 3;
    public static final String DRIVE_MASTER_FOLDER = "Zecorder";
    public static final String STREAM_PROFILE = "Stream_Profile";
    public static final String ACTION_NOTIFY_FROM_STREAM_SERVICE = "ACTION_NOTIFY_FROM_STREAM_SERVICE";
    private static final String TAG = "chienpm_utils";

    @NonNull
    public static String createFileName(@NonNull String ext) {
        return "Zecorder-" +
                Long.toHexString(System.currentTimeMillis()) + ext;
    }

    @NonNull
    public static String getBaseStorageDirectory() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + "/Zecorder";
    }

    public static void showSnackBarNotification(View view, String msg, int length) {
        Snackbar.make(view, msg, length).show();
    }

    public static void toast(Context mContext, String msg, int length) {
        Toast.makeText(mContext, msg, length).show();
    }

    public static boolean isValidFilenameSynctax(String filename) {
        for(int i = 0; i< filename.length(); i++){
            char c = filename.charAt(i);
            if(c == '/' || c =='\\' || c=='"' || c == ':' || c=='*'||c=='<'|| c =='>' || c == '|')
                return true;
        }
        return false;
    }

    public static void saveCanvas(Canvas canvas) {

    }

    public static void logBytes(String msg, byte[] bytes) {
        StringBuilder str = new StringBuilder(msg + ": "+bytes.length+"\n");

        if(bytes == null || bytes.length < 1)
            str.append("bytes is null or length < 1");
        else{

            String base64Encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
            str.append("\nbase64: ").append(base64Encoded);//.append("\nbytes: ");
//            for(int i = 0; i < bytes.length; i++){
////                str.append( bytes[i]).append(" ");
//                str.append(String.format("%02x ", bytes[i]));
//            }
//            str.append(getHex(bytes));

        }
        Log.i(TAG, str.toString());
    }

    static final String HEXES = "0123456789ABCDEF";
    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F))).append(' ');
        }
        return hex.toString();
    }

    public static void shootPicture(ByteBuffer buf, int mWidth, int mHeight) {

        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;

        File captureFile = new File(MyUtils.getBaseStorageDirectory(), MyUtils.createFileName(".jpg"));

        if (!captureFile.getParentFile().exists()) {
            captureFile.getParentFile().mkdirs();
        }

        if (captureFile.toString().endsWith(".jpg")) {
            compressFormat = Bitmap.CompressFormat.JPEG;
        }
        BufferedOutputStream os = null;
        try {
            try {
                os = new BufferedOutputStream(new FileOutputStream(captureFile));
                final Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                buf.clear();
                bmp.copyPixelsFromBuffer(buf);
                bmp.compress(compressFormat, 100, os);

                //Log
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.i(TAG, "shootPicture: "+encoded);
                //endlog

                bmp.recycle();
                os.flush();
            } finally {
                if (os != null) os.close();
            }
        } catch (final FileNotFoundException e) {
            Log.w(TAG, "failed to save file", e);
        } catch (final IOException e) {
            Log.w(TAG, "failed to save file", e);
        }
    }

    public static boolean isRunningServices(Context context, Class<SyncService> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
