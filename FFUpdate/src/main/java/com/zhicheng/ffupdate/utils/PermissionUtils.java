package com.zhicheng.ffupdate.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Name:    PermissionUtils
 * Author:  wuzhicheng
 * Time:    2019/1/10  18:21
 * Version: 1.0
 * Description: this is PermissionUtils class.
 */
public class PermissionUtils {

    private static final String TAG = "PermissionUtils";

    public static boolean hasFilePermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasNetPermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity,Manifest.permission.INTERNET)==PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAuth(Activity activity){
        boolean b = SPUtils.init(activity).isRequestPermission();
        if (b) return;
        if (Build.VERSION.SDK_INT < 23){
            //无需申请动态权限
            return;
        }
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.REQUEST_INSTALL_PACKAGES
        };
        ActivityCompat.requestPermissions(activity,permissions,100);
        SPUtils.init(activity).setIsRequestPermission(true).save();
    }


    public static void goSetting(Activity activity){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
