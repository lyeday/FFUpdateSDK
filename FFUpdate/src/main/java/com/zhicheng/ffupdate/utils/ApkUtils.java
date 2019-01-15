package com.zhicheng.ffupdate.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;

/**
 * Name:    ApkUtils
 * Author:  wuzhicheng
 * Time:    2019/1/10  16:45
 * Version: 1.0
 * Description: this is ApkUtils class.
 */
public class ApkUtils {
    private static final String TAG = "ApkUtils";

    public static boolean installApk(Context context, File apk){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri = null;
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                apkUri = FileUtils.uriFromFile(context,apk);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }else{
                apkUri = Uri.fromFile(apk);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setDataAndType(apkUri,"application/vnd.android.package-archive");
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
