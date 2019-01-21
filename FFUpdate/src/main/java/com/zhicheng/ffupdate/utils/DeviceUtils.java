package com.zhicheng.ffupdate.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Name:    DeviceUtils
 * Author:  wuzhicheng
 * Time:    2019/1/17  11:30
 * Version: 1.0
 * Description: this is DeviceUtils class.
 */
public class DeviceUtils {


    private static final String TAG = "DeviceUtils";

    /**
     * 获取系统版本
     * @return
     */
    public static String getSyetemVersion(){
        String anInt = Build.VERSION.RELEASE;
        return anInt;
    }

    /**
     * 获取手机型号
     * @return
     */
    public static String getModel(){
        String model = Build.MODEL;
        return model;
    }

    /**
     * 获取厂商
     * @return
     */
    public static String getBrand(){
        String brand = Build.BRAND;
        return brand;
    }

    /**
     * 获取手机名称
     * @return
     */
    public static String getPhoneName(){
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        if (myDevice == null) return Build.MANUFACTURER;
        String deviceName = myDevice.getName();
        return deviceName;
    }

    /**
     * 获取设备唯一标识
     * @param context
     * @return
     */
    public static String getUDID(Context context){
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return id;
        }
    }

    /**
     * MD5 加密算法
     * @param text
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }
}
