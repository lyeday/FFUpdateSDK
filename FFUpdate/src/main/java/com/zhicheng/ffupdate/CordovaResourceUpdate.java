package com.zhicheng.ffupdate;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.zhicheng.ffupdate.activity.ResourceUpdateActivity;
import com.zhicheng.ffupdate.dialog.FFAlertDialog;
import com.zhicheng.ffupdate.net.FFNetwork;
import com.zhicheng.ffupdate.utils.PermissionUtils;
import com.zhicheng.ffupdate.utils.SPUtils;
import com.zhicheng.ffupdate.utils.UpdateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Name:    CordovaResourceUpdate
 * Author:  wuzhicheng
 * Time:    2019/1/11  09:26
 * Version: 1.0
 * Description: this is CordovaResourceUpdate class.
 */
public class CordovaResourceUpdate implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "CordovaResourceUpdate";
    private static CordovaResourceUpdate shareObj = new CordovaResourceUpdate();

    private String appKey;

    private Context mContext;

    private Activity mCurrentActivity;

    private int id = 0;

    private String upMsg = "";
    private String mIndex = "";
    private int mVersion = 0;

    private boolean forceUpdate = false;

    private Handler mHandle = new Handler();

    public static CordovaResourceUpdate shareUpdate(){
        if (shareObj == null){
            shareObj = new CordovaResourceUpdate();
        }
        return shareObj;
    }

    public void registerKey(String key, Application application){
        appKey = key;
        mContext = application.getApplicationContext();
        application.registerActivityLifecycleCallbacks(shareObj);
        if (UpdateUtils.AUTHORITY == null){
            UpdateUtils.AUTHORITY = application.getPackageName()+".ffupdate.fileprovider";
        }
    }

    public void setCurrentResourceVersion(int version){
        SPUtils spUtils = SPUtils.init(mContext);
        if (version > spUtils.appResourceVersion())
        spUtils.setAppResourceVersion(version).save();
    }

    public void restartApplication(){
        Intent intentForPackage = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        intentForPackage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intentForPackage);
    }

    public void checkUpdate(){
        if (mCurrentActivity == null || FFUpdate.shareUpdate().isUpdateing()){
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUpdate();
                }
            },10000);
            return;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("platform","android");
        params.put("version", SPUtils.init(mContext).appVersion());
        params.put("appkey",appKey);
        final int version = SPUtils.init(mContext).appResourceVersion();
        FFNetwork.POST("appWeb.php/app/checkhtml", params, new FFNetwork.FFNetworkCallback() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                Log.i(TAG, "onSuccess: 请求结果:"+data);
                if (code == 0){
                    JSONObject jsonObject = (JSONObject) data;
                    try {
                        final int min = Integer.parseInt(jsonObject.get("min").toString());
                        final int current = Integer.parseInt(jsonObject.get("current").toString());
                        id = Integer.parseInt(jsonObject.get("id").toString());
                        upMsg = jsonObject.get("msg").toString();
                        mIndex = jsonObject.get("index").toString();
                        mVersion = current;
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                if (version < min || version > current){//强制更新
                                    forceUpdate = true;
                                    updateResource();
                                    return;
                                }
                                if (version<current){ //推荐更新
                                    forceUpdate = false;
                                    updateResource();
                                    return;
                                }
                            }
                        });

                        Log.i(TAG, "onSuccess: 资源已是最新版本");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError() {
                Log.i(TAG, "onError: 请求失败.");
            }
        });
    }


    /**
     * 初始化cordova 配置
     * @param activity
     */
    public void initCordovaConfig(Activity activity){
        String resourceIndex = SPUtils.init(activity).appResourceIndex();
        File wwwDir = UpdateUtils.wwwDir(activity);
        File file = null;
        if (resourceIndex != null) {
            file = new File(wwwDir, resourceIndex);
        }
        String indexPage = "";
        try {
            Field launchUrl = Class.forName("org.apache.cordova.CordovaActivity").getDeclaredField("launchUrl");
            launchUrl.setAccessible(true);
            String url = (String)launchUrl.get(activity);
            Log.i(TAG, "initCordovaConfig: "+url);
            indexPage = url;
        }catch (Exception e){
            e.printStackTrace();
        }
        if (wwwDir.exists()==false){
            SPUtils.init(activity).setAppResourceVersion(1);
        }
        if (resourceIndex !=null && file.exists()){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                indexPage = "file://"+file.getAbsolutePath();
            }else {
                indexPage = UpdateUtils.cordovaWWWDir(activity) + File.separator+resourceIndex;
            }
        }
        try {
            Method loadUrl = activity.getClass().getMethod("loadUrl",String.class);
            loadUrl.invoke(activity,indexPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateResource(){
        if (mCurrentActivity.isDestroyed()) return;
        FFAlertDialog alertDialog = new FFAlertDialog(mCurrentActivity)
                .setTitle("发现新的升级包")
                .setMessage(upMsg + "")
                .setCancelAble(false)
                .setPositiveButton("立即更新", new FFAlertDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialog, int i) {
                        gotoUpdateActivity();
                    }
                });
        if (!forceUpdate){
            alertDialog.setNegativeButton("下次再说", new FFAlertDialog.OnClickListener() {
                @Override
                public void onClick(Dialog dialog, int i) {
                }
            });
        }
        alertDialog.show();
    }

    private void gotoUpdateActivity(){
        Intent intent = new Intent(mCurrentActivity, ResourceUpdateActivity.class);
        intent.putExtra("id",id+"");
        intent.putExtra("msg",upMsg+"");
        intent.putExtra("version",mVersion);
        intent.putExtra("index",mIndex);
        mCurrentActivity.startActivity(intent);
    }


    /******************** ActivityLifecycleCallbacks ************************/
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        PermissionUtils.requestAuth(activity);
        mCurrentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
