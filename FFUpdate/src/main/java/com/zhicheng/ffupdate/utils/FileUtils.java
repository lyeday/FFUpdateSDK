package com.zhicheng.ffupdate.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.zhicheng.ffupdate.provider.FFFileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Name:    FileUtils
 * Author:  wuzhicheng
 * Time:    2019/1/11  14:41
 * Version: 1.0
 * Description: this is FileUtils class.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";


    private static void copyFile(File src, File to) throws Exception {
        if (to.exists() == false) to.createNewFile();
        FileInputStream inputStream = new FileInputStream(src);
        FileOutputStream fileOutputStream = new FileOutputStream(to);
        int len = 0;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer))>0){
            fileOutputStream.write(buffer,0,len);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();
    }

    public static boolean copy(File srcDir,File toDir){
        try {
            if (srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();
                for (File file : files) {
                    String name = file.getName();
                    if (file.isDirectory()) {
                        File toFileDir = new File(toDir, name);
                        toFileDir.mkdirs();
                        copy(file, toFileDir);
                    }else{
                        File toFile = new File(toDir, name);
                        copyFile(file,toFile);
                    }
                }
            }else {
                File file = new File(toDir, srcDir.getName());
                copyFile(srcDir,file);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void deleteDir(File file){
        if (file.exists()==false) return;
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.isDirectory()){
                    deleteDir(subFile);
                }else {
                    subFile.delete();
                }
            }
        }
        file.delete();
    }

    public static Uri uriFromFile(Context context, File file){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            return Uri.fromFile(file);
        }
        return FFFileProvider.getUriForFile(context,UpdateUtils.AUTHORITY,file);
    }
}
