package com.zhicheng.ffupdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.zhicheng.ffupdate.dialog.FFAlertDialog;
import com.zhicheng.ffupdate.net.FFNetwork;
import com.zhicheng.ffupdate.utils.ApkUtils;
import com.zhicheng.ffupdate.utils.PackageUtils;
import com.zhicheng.ffupdate.utils.PermissionUtils;
import com.zhicheng.ffupdate.utils.SPUtils;
import com.zhicheng.ffupdate.utils.UpdateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * Name:    FFUpdate
 * Author:  wuzhicheng
 * Time:    2019/1/9  14:40
 * Version: 1.0
 * Description: this is FFUpdate class.
 */
public class FFUpdate implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "FFUpdate";
    private static FFUpdate shareObj = new FFUpdate();
    private String mAppkey = "";
    private String install = "";
    private Activity currentActivity;
    private File apkFile;
    private boolean mForceUpdate = false; //是否为强制更新

    private Application mContext;
    private boolean updateing = false;

    private Handler mHandle = new Handler();


    interface UpdateCallback{
        /**
         * 当强制自动更新时候回调
         */
        void onAutoUpdate();

        /**
         * 当需要更新时候回调
         * @param needUpdate 是否需要更新
         */
        void onSuccess(boolean needUpdate);

        /**
         * 当更新出错时回调
         */
        void onError();
    }

    public static FFUpdate shareUpdate(){
        if (shareObj == null){
            shareObj = new FFUpdate();
        }
        return shareObj;
    }

    /**
     * 注册key
     * @param appkey
     */
    public void registerAppKey(String appkey,Application context){
        mAppkey = appkey;
        mContext = context;
        mContext.registerActivityLifecycleCallbacks(this);
        if (UpdateUtils.AUTHORITY == null){
            UpdateUtils.AUTHORITY = context.getPackageName()+".ffupdate.fileprovider";
        }
    }

    /**
     * 检查更新
     */
    public void checkUpdate(){
        updateing = true;
        checkUpdateWithCallback(null);
    }


    private void checkUpdateWithCallback(final UpdateCallback callback){
        Boolean firstInstall = false;
        int readyVersion = SPUtils.init(mContext).appReadyVersion();
        switch (PackageUtils.apkInstallStatus(mContext)){
            case PackageUtils.STATUS_FIRST_INSTALL:
                firstInstall = true;
                Log.i(TAG, "checkUpdateWithCallback: 第一次安装...");
                break;
            case PackageUtils.STATUS_RE_INSTALL: //新安装了app
                SPUtils.init(mContext)
                        .setAppLastInstall(PackageUtils.lastUpdateTime(mContext))
                        .setAppVersion(readyVersion).save();
                Log.i(TAG, "checkUpdateWithCallback: 更新安装...");
                break;
            case PackageUtils.STATUS_NO_INSTALL: //没有安装动作
                Log.i(TAG, "checkUpdateWithCallback: 没有检测到安装");
                break;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("platform","android");
        params.put("appkey",mAppkey);
        final Boolean finalFirstInstall = firstInstall;
        FFNetwork.POST("appWeb.php/app/checkversion", params, new FFNetwork.FFNetworkCallback() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                Log.i(TAG, "onSuccess: "+data);
                if (code == 0){
                    final JSONObject jsonObject = (JSONObject) data;
                    try {
                        SPUtils spUtils = SPUtils.init(mContext);
                        install = jsonObject.getString("install");
                        final int current = jsonObject.getInt("current");
                        final int min = jsonObject.getInt("min");
                        if (finalFirstInstall){
                                    spUtils
                                    .setAppLastInstall(PackageUtils.lastUpdateTime(mContext))
                                    .setAppReadyVersion(current)
                                    .setAppVersion(current)
                                    .save();
                            return;
                        }
                        final int localVersion = spUtils.appVersion();
                        spUtils.setAppReadyVersion(current).save();
                        Log.i(TAG, "onSuccess: 本地版本号:"+localVersion);
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                if (localVersion < min){//强制更新
                                    if (callback!=null)
                                    callback.onAutoUpdate();
                                    mForceUpdate = true;
                                    updateApk(jsonObject);
                                    return;
                                }
                                if (localVersion < current){//推荐更新
                                    if (callback != null){
                                        callback.onSuccess(true);
                                        return;
                                    }
                                    mForceUpdate = false;
                                    updateApk(jsonObject);
                                    return;
                                }
                                if (callback != null){
                                    callback.onSuccess(false);
                                    return;
                                }
                                updateing = false;
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (callback!=null)
                        callback.onError();
                        updateing = false;
                    }
                }
            }

            @Override
            public void onError() {
                if (callback!=null)
                callback.onError();
                updateing = false;
            }
        });
    }

    private void gotoWebpageDownload(){
        Uri uri = Uri.parse(install);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        currentActivity.startActivity(intent);
        if (mForceUpdate){
            downloadFaild();
        }
    }

    /**
     * 下载成功,安装更新
     */
    private void downloadSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("提示");
        builder.setMessage("如果安装失败,请在文件夹'"+mContext.getPackageName()+"'中手动点击'"+apkFile.getName()+"'完成安装应用");
        builder.setCancelable(false);
        if (!mForceUpdate){
            builder.setNegativeButton("下次安装", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.setPositiveButton("尝试自动安装", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                downloadSuccess();
            }
        });
        builder.show();
        if (apkFile.exists()) {
            ApkUtils.installApk(currentActivity, apkFile);
        }else {
            downloadFaild();
        }
    }

    /**
     * 下载失败,启用网页下载方式安装更新
     */
    private void downloadFaild(){
        AlertDialog.Builder downloadAlert = new AlertDialog.Builder(currentActivity)
                .setTitle("下载失败")
                .setMessage("请前往浏览器下载该应用"+(mForceUpdate?"或者再试一次":"或者下次启动时候更新"))
                .setCancelable(false);
        if (mForceUpdate){
            downloadAlert.setNegativeButton("前往下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    gotoWebpageDownload();
                }
            });

            downloadAlert.setPositiveButton("再试一次", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dowloadApk();
                }
            });
        }else {
            downloadAlert.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            downloadAlert.setPositiveButton("前往下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    gotoWebpageDownload();
                }
            });
        }
        downloadAlert.show();
    }

    /**
     * 下载更新
     */
    private void dowloadApk(){
        if (!PermissionUtils.hasFilePermission(currentActivity)){
            AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
                    .setTitle("没有文件写入权限")
                    .setMessage("请打开文件读取权限后再尝试 ~")
                    .setCancelable(false)
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PermissionUtils.goSetting(currentActivity);
                            dialogInterface.dismiss();
                            dowloadApk();
                        }
                    });
            if (!mForceUpdate){
                builder.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }
            builder.show();
            return;
        }
        apkFile = new File(Environment.getExternalStorageDirectory()+File.separator+mContext.getPackageName(),PackageUtils.getAppName(mContext)+".apk");
        final ProgressDialog progressDialog = new ProgressDialog(currentActivity);
        progressDialog.setTitle("正在下载更新...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (!apkFile.getParentFile().exists()){
            apkFile.getParentFile().mkdirs();
        }
        if (apkFile.exists()){
            apkFile.delete();
        }
        FFNetwork.download(install,apkFile, new FFNetwork.FFDownloadCallback() {
            @Override
            public void progress(final long complete, final long total) {
                mHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        int pro = (int)(complete*1.0f / total * 100);
                        progressDialog.setProgress(pro);
                    }
                });
            }

            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: 下载成功");
                //下载成功
                mHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        downloadSuccess();
                    }
                });
            }

            @Override
            public void onError() {
                Log.i(TAG, "onError: 下载失败");
                //下载失败
                mHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        downloadFaild();
                    }
                });
            }
        });
    }

    private void updateApk(final JSONObject data){
        if (currentActivity==null)  {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateApk(data);
                }
            },5000);
            return;
        }
        String msg = "";
        try {
            msg = data.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FFAlertDialog alertDialog = new FFAlertDialog(currentActivity)
                .setTitle("发现新版本")
                .setMessage(msg)
                .setCancelAble(false)
                .setPositiveButton("立即更新", new FFAlertDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialog, int i) {
                        dowloadApk();
                    }
                });
//        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
//                .setTitle("发现新版本")
//                .setMessage(msg)
//                .setCancelable(false)
//                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dowloadApk();
//                    }
//                });
        if (mForceUpdate == false){
            alertDialog.setNegativeButton("下次再说", new FFAlertDialog.OnClickListener() {
                @Override
                public void onClick(Dialog dialog, int i) {
                    updateing = false;
                }
            });
        }

        alertDialog.show();
    }


    public boolean isUpdateing(){
        return this.updateing;
    }
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        PermissionUtils.requestAuth(activity);
        currentActivity = activity;
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
