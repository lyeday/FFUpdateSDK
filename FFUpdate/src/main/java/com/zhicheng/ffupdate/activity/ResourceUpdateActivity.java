package com.zhicheng.ffupdate.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhicheng.ffupdate.CordovaResourceUpdate;
import com.zhicheng.ffupdate.R;
import com.zhicheng.ffupdate.net.FFNetwork;
import com.zhicheng.ffupdate.utils.DeviceUtils;
import com.zhicheng.ffupdate.utils.FileUtils;
import com.zhicheng.ffupdate.utils.SPUtils;
import com.zhicheng.ffupdate.utils.UpdateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ResourceUpdateActivity extends Activity {
    private static final String TAG = "ResourceUpdateActivity";
    private static final int UPDATE_PROGRESS = 0;

    private Toast mToast;
    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mProgressView;
    private ProgressBar mProgressBarView;
    private TextView mUpcontentView;

    File mUnzipDirFile; //文件解压的路径

    private String id = "";
    private String msg = "";
    private String index = "";
    private int version = 0;

    private Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PROGRESS:
                {
                    int progress = (int) msg.obj;
                    mProgressBarView.setProgress(progress);
                    mProgressView.setText(String.format("%d%%",progress));
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_resource_update);
        initData();
        initUI();
        downloadResource();
    }

    private void initData() {
        id = getIntent().getStringExtra("id");
        msg = getIntent().getStringExtra("msg");
        index = getIntent().getStringExtra("index");
        version = getIntent().getIntExtra("version",0);
    }

    private void downloadResource() {
        File zipFile = UpdateUtils.zipFile(this);
        if (zipFile.exists()){
            zipFile.delete();
        }
        mTitleView.setText("正在下载资源文件...");
        mProgressBarView.setProgress(0);
        mProgressView.setText("0%");
        FFNetwork.download(UpdateUtils.BASE_URL+"appWeb.php/app/downloadUpdate?id="+id, zipFile, new FFNetwork.FFDownloadCallback() {
            @Override
            public void progress(final long complete, final long total) {
                mHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        float progress = complete*1.0f/total*100.0f;
                        mProgressBarView.setProgress((int)progress);
                        mProgressView.setText(String.format("%d%%",(int)progress));
                    }
                });
            }

            @Override
            public void onSuccess() {

                unZipFile();
            }

            @Override
            public void onError() {

            }
        });
    }

    private void unZipFile(){
        mHandle.post(new Runnable() {
            @Override
            public void run() {
                mTitleView.setText("正在释放资源文件...");
                mProgressBarView.setProgress(0);
                mProgressView.setText("0%");
            }
        });
        File zipFile = UpdateUtils.zipFile(this);
        File unzipDirFile = UpdateUtils.unzipTempDir(this);
        FileUtils.deleteDir(unzipDirFile);
        unzipDirFile.mkdirs();
        mUnzipDirFile = unzipDirFile;
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(zipFile);
            zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = null;
            int readLen = 0;
            long totalUnzip = 0;
            long total = 0;
            byte[] buff = new byte[1024];
            ZipFile zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()){
                total+=entries.nextElement().getSize();
            }
            while ((zipEntry = zipInputStream.getNextEntry())!=null){
                String zipEntryName = zipEntry.getName();
                if (zipEntry.isDirectory()){
                    File zipDir = new File(unzipDirFile, zipEntryName);
                    if (!zipDir.exists()){
                        zipDir.mkdirs();
                    }
                }else{
                    File file = new File(unzipDirFile, zipEntryName);
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    while ((readLen = zipInputStream.read(buff)) > 0){
                        fileOutputStream.write(buff,0,readLen);
                        totalUnzip += readLen;
                        int progress = (int)(totalUnzip*100/total);
                        Message message = new Message();
                        message.what = UPDATE_PROGRESS;
                        message.obj = progress;
                        mHandle.sendMessage(message);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                zipInputStream.closeEntry();
            }
            unzipSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            unzipFaild();
        }
        try {
             if (fileInputStream!=null) fileInputStream.close();
            if (zipInputStream!=null) zipInputStream.close();
            if (fileOutputStream!=null) fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void unzipFaild(){
        showToast("解压失败");
    }

    private void unzipSuccess(){
        mHandle.post(new Runnable() {
            @Override
            public void run() {
                DeviceUtils.reportInstall(ResourceUpdateActivity.this,1,CordovaResourceUpdate.shareUpdate().getAppkey(),version,1);
                mTitleView.setText("正在应用资源文件...");
                mProgressBarView.setProgress(0);
                mProgressView.setText("0%");
            }
        });
        File file = new File(mUnzipDirFile, index);
        if (file.exists()){
            Log.i(TAG, "unzipSuccess: 文件存在:"+file.getPath());
            File wwwDir = UpdateUtils.wwwDir(this);
            FileUtils.deleteDir(wwwDir);
            wwwDir.mkdirs();
            //拷贝assets文件到www目录
            copyAssets("www",wwwDir.getPath());
            final boolean copy = FileUtils.copy(mUnzipDirFile, wwwDir);
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    if (copy){
                        mProgressBarView.setProgress(100);
                        mProgressView.setText("100%");
                        SPUtils.init(ResourceUpdateActivity.this)
                                .setAppResourceVersion(version)
                                .setAppResourceIndex(index)
                                .save();
                        new AlertDialog.Builder(ResourceUpdateActivity.this)
                                .setTitle("升级成功")
                                .setMessage("请重新打开应用体验新版本")
                                .setCancelable(false)
                                .setPositiveButton("立即体验", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                        CordovaResourceUpdate.shareUpdate().restartApplication();
                                    }
                                }).show();
                    }else {
                        new AlertDialog.Builder(ResourceUpdateActivity.this)
                                .setTitle("升级失败")
                                .setMessage("资源包升级失败,请与开发者联系")
                                .setCancelable(false)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).show();
                    }
                }
            });
        }else{
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    SPUtils.init(ResourceUpdateActivity.this).setAppResourceVersion(version).save();
                    new AlertDialog.Builder(ResourceUpdateActivity.this)
                            .setTitle("提示")
                            .setMessage("没有发现入口文件,请与开发者联系")
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).show();
                }
            });
        }
    }

    private void initUI() {
        mImageView = findViewById(R.id.iv_update);
        mTitleView = findViewById(R.id.tv_title);
        mProgressView = findViewById(R.id.tv_progress);
        mProgressBarView = findViewById(R.id.pb_progressBar);
        mUpcontentView = findViewById(R.id.tv_up_content);

        mUpcontentView.setText(msg);
        mProgressBarView.setProgress(0);
        mProgressBarView.setMax(100);

        RotateAnimation animation = new RotateAnimation(0,359, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(3800);
        animation.setRepeatCount(-1);
        animation.setFillAfter(true);
        animation.setInterpolator(new LinearInterpolator());
        mImageView.setAnimation(animation);
    }

    /**
     * 拷贝原始资源文件
     * @param dirName www
     * @param toPath 目标文件夹
     */
    private void copyAssets(String dirName,String toPath) {
        AssetManager assetManager = getResources().getAssets();
        try {
            String[] wwwList = assetManager.list(dirName);
            for (String www : wwwList) {
                String subPath = dirName + File.separator + www;
                File toFile = new File(toPath, www);
                String[] list = assetManager.list(subPath);
                if (list.length>0){ //文件夹
                    toFile.mkdirs();
                    copyAssets(subPath,toFile.getPath());
                }else{//文件
                    InputStream open = assetManager.open(subPath);
                    FileOutputStream fileOutputStream = new FileOutputStream(toFile);
                    byte buffer[] = new byte[1024];
                    int readLen = 0;
                    while ((readLen = open.read(buffer))>=0){
                        fileOutputStream.write(buffer,0,readLen);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    open.close();
                    buffer = null;
                    final int progress = mProgressBarView.getProgress();
                    if (progress<98) {
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBarView.setProgress(progress+1);
                                mProgressView.setText((progress+1)+"%");
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        showToast("请耐心等待更新完成");
//        super.onBackPressed();
    }

    private void showToast(String msg){
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setText(msg);
        mToast.show();
    }
}
