package com.zhicheng.ffupdate.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Name:    UpdateUtils
 * Author:  wuzhicheng
 * Time:    2019/1/11  11:00
 * Version: 1.0
 * Description: this is UpdateUtils class.
 */
public class UpdateUtils {

//    public static String BASE_URL = "http://192.168.1.188/apps/";
    public static String BASE_URL = "https://www.jssgwl.com/apps/";
    public static String AUTHORITY = null;

    public static File zipTempDir(Context context){
        File dirFile = new File(Environment.getExternalStorageDirectory() + File.separator + ".FF_TEMP" + File.separator + context.getPackageName());
        if (dirFile.exists() == false) dirFile.mkdirs();
        return dirFile;
    }

    public static File unzipTempDir(Context context){
        File dirFile = new File(zipTempDir(context) + File.separator + "update");
        return dirFile;
    }

    public static File zipFile(Context context){
        return new File(zipTempDir(context),"update.zip");
    }

    /**
     * 获取www目录
     * @param context
     * @return
     */
    public static File wwwDir(Context context){
        File dirFile = new File(Environment.getExternalStorageDirectory() + File.separator + ".www."+context.getPackageName());
        if (dirFile.exists() == false) dirFile.mkdirs();
        return dirFile;
    }

    public static String cordovaWWWDir(Context context){
        return "file:///mnt/sdcard/.www."+context.getPackageName();
    }

}
