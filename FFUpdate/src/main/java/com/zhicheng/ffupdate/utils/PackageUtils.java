package com.zhicheng.ffupdate.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Name:    PackageUtils
 * Author:  wuzhicheng
 * Time:    2019/1/9  16:47
 * Version: 1.0
 * Description: this is PackageUtils class.
 */
public class PackageUtils {

    public static final int STATUS_FIRST_INSTALL = 0;//第一次安装
    public static final int STATUS_RE_INSTALL = 1;   //重新安装
    public static final int STATUS_NO_INSTALL = 2;   //无更新

    /**
     * 获取最后安装时间
     * @param context
     * @return
     */
    public static long lastUpdateTime(Context context){
        try {
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
            return lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取第一次安装时间
     * @param context
     * @return
     */
    public static long firstInstallTime(Context context){
        try {
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            return lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取应用安装情况
     * @param context
     * @return
     */
    public static int apkInstallStatus(Context context){
        if (SPUtils.init(context).lastInstallTime() == 0){
            return STATUS_FIRST_INSTALL;
        }
        if (lastUpdateTime(context) == SPUtils.init(context).lastInstallTime()){
            return STATUS_NO_INSTALL;
        }
        return STATUS_RE_INSTALL;
    }


    public static String getAppName(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            String appName = packageManager.getApplicationLabel(applicationInfo).toString();
            return appName;
        } catch (Exception e) {
            e.printStackTrace();
            return context.getPackageName();
        }
    }

}
