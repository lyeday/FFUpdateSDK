package com.zhicheng.ffupdate.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Name:    SPUtils
 * Author:  wuzhicheng
 * Time:    2019/1/9  16:28
 * Version: 1.0
 * Description: this is SPUtils class.
 */
public class SPUtils {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edit;

    private static final String KEY_APP_VERSION = "KEY_APP_VERSION";
    private static final String KEY_APP_LAST_INSTALL = "KEY_APP_LAST_INSTALL";
    private static final String KEY_APP_REDAY_VERSION = "KEY_APP_REDAY_VERSION";
    private static final String KEY_APP_PERMISS_REQUEST = "KEY_APP_PERMISS_REQUEST";
    private static final String KEY_APP_RESOURCE_VERSION = "KEY_APP_RESOURCE_VERSION";
    private static final String KEY_APP_RESOURCE_INDEX = "KEY_APP_RESOURCE_INDEX";

    public static SPUtils init(Context context){
        SPUtils spUtils = new SPUtils();
        spUtils.sharedPreferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        spUtils.edit = spUtils.sharedPreferences.edit();
        return spUtils;
    }

    public SPUtils setAppVersion(int appVersion){
        edit.putInt(KEY_APP_VERSION,appVersion);
        return this;
    }

    public SPUtils setAppLastInstall(long lastInstall){
        edit.putLong(KEY_APP_LAST_INSTALL,lastInstall);
        return this;
    }

    public SPUtils setAppReadyVersion(int readyVersion){
        edit.putInt(KEY_APP_REDAY_VERSION,readyVersion);
        return this;
    }

    public SPUtils setIsRequestPermission(boolean b){
        edit.putBoolean(KEY_APP_PERMISS_REQUEST,b);
        return this;
    }

    public SPUtils setAppResourceVersion(int version){
        edit.putInt(KEY_APP_RESOURCE_VERSION,version);
        return this;
    }

    public SPUtils setAppResourceIndex(String index){
        edit.putString(KEY_APP_RESOURCE_INDEX,index);
        return this;
    }

    public void save(){
        edit.commit();
    }

    public long lastInstallTime(){
        return sharedPreferences.getLong(KEY_APP_LAST_INSTALL,0);
    }

    public int appReadyVersion(){
        return sharedPreferences.getInt(KEY_APP_REDAY_VERSION,0);
    }

    public int appVersion(){
        return sharedPreferences.getInt(KEY_APP_VERSION,0);
    }

    public boolean isRequestPermission(){
        return sharedPreferences.getBoolean(KEY_APP_PERMISS_REQUEST,false);
    }

    public int appResourceVersion(){
        return sharedPreferences.getInt(KEY_APP_RESOURCE_VERSION,0);
    }

    public String appResourceIndex(){
        return sharedPreferences.getString(KEY_APP_RESOURCE_INDEX,null);
    }


}
