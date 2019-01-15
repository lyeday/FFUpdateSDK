package com.zhicheng.ffupdate.net;

import android.util.Log;

import com.zhicheng.ffupdate.utils.UpdateUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Name:    FFNetwork
 * Author:  wuzhicheng
 * Time:    2019/1/9  14:40
 * Version: 1.0
 * Description: this is FFNetwork class.
 */
public class FFNetwork {
    private static final String TAG = "FFNetwork";

    public interface FFNetworkCallback{
        void onSuccess(int code, String msg, Object data);
        void onError();
    }
    public interface FFDownloadCallback{
        void progress(long complete, long total);
        void onSuccess();
        void onError();
    }

    private static TrustManager[]  trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }
    };

    private static HostnameVerifier NOT_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    public static void POST(String uri, Map params,FFNetworkCallback callback){
        new Thread(
                new POSTRunable(uri,params,callback)
        ).start();
    }

    public static void download(String uri,File apkFile,FFDownloadCallback callback){
        new Thread(
            new DownloadRunable(uri, apkFile,callback)
        ).start();
    }

    private static class DownloadRunable implements Runnable{
        private String uri;
        private File file;
        private FFDownloadCallback callback;

        public DownloadRunable(String uri, File file, FFDownloadCallback callback) {
            this.uri = uri;
            this.file = file;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(uri);//new URL("https://192.168.1.188/xihuanni.mp4");//
                Log.i(TAG, "run: 下载文件:"+url);
                URLConnection connection = url.openConnection();
                if (uri.startsWith("https")){
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                    httpsURLConnection.setHostnameVerifier(NOT_VERIFIER);
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null,trustAllCerts,new SecureRandom());
                    httpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
                    httpsURLConnection.setRequestMethod("GET");
                    httpsURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpsURLConnection.setConnectTimeout(30000);
                    httpsURLConnection.setReadTimeout(30000);
                }else{
                    HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                    httpURLConnection.setDoOutput(true);// 使用 URL 连接进行输出
                    httpURLConnection.setDoInput(true);// 使用 URL 连接进行输入
                    httpURLConnection.setUseCaches(false);// 忽略缓存
                    httpURLConnection.setConnectTimeout(30000);
                    httpURLConnection.setReadTimeout(30000);
                }
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                long contentLength = connection.getContentLength();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte buff[] = new byte[2048];
                int len;
                long complete = 0;
                while ((len=inputStream.read(buff))!=-1){
                    fileOutputStream.write(buff,0,len);
                    complete+=len;
                    callback.progress(complete,contentLength);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                callback.progress(contentLength,contentLength);
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "run: 报错信息");
                callback.onError();
            }
        }
    }

    /**
     * POST 请求的线程
     */
    private static class POSTRunable implements Runnable{
        private String uri;
        private Map params;
        private FFNetworkCallback callback;

        public POSTRunable(String uri, Map params,FFNetworkCallback callback) {
            this.uri = uri;
            this.params = params;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(UpdateUtils.BASE_URL + uri);
                Log.i(TAG, "run: 请求地址:"+url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (UpdateUtils.BASE_URL.startsWith("https")){
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                    httpsURLConnection.setHostnameVerifier(NOT_VERIFIER);
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null,trustAllCerts,new SecureRandom());
                    httpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
                }
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                if (params!=null) {
                    OutputStream outputStream = connection.getOutputStream();
                    StringBuffer paramsStrings = new StringBuffer();
                    boolean first = true;
                    for (Object key : params.keySet()) {
                        if (!first){
                            paramsStrings.append("&");
                        }
                        Object obj = params.get(key);
                        paramsStrings.append(key+"="+obj);
                        first = false;
                    }
                    outputStream.write(paramsStrings.toString().getBytes());
                    outputStream.flush();
                    outputStream.close();
                }
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    String line = "";
                    while ((line=bufferedReader.readLine())!=null){
                        stringBuffer.append(line);
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();
                    JSONObject jsonObject = new JSONObject(stringBuffer.toString());
                    int code = Integer.parseInt(jsonObject.get("code").toString());
                    String msg = jsonObject.get("message").toString();
                    Object data = jsonObject.get("data");
                    callback.onSuccess(code,msg,data);
                }else{
                    callback.onError();
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError();
            }
        }
    }
}
