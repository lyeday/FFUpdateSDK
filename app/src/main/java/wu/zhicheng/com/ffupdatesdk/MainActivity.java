package wu.zhicheng.com.ffupdatesdk;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhicheng.ffupdate.CordovaResourceUpdate;
import com.zhicheng.ffupdate.FFUpdate;
import com.zhicheng.ffupdate.utils.DeviceUtils;
import com.zhicheng.ffupdate.utils.SPUtils;
import com.zhicheng.ffupdate.utils.UpdateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//./gradlew clean build bintrayUpload -PbintrayUser=voisen -PbintrayKey= -PdryRun=false
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdateUtils.BASE_URL = "https://www.jssgwl.com/appstest/";
        SPUtils.init(this).setAppVersion(10).setAppReadyVersion(0).setAppResourceVersion(0).save();
        FFUpdate.shareUpdate().registerAppKey("kYvTNZmzD1kSzlSiKVmRuR8sU2U9vs5j",getApplication());
        FFUpdate.shareUpdate().checkUpdate();
        CordovaResourceUpdate.shareUpdate().registerKey("kYvTNZmzD1kSzlSiKVmRuR8sU2U9vs5j",getApplication());
        findViewById(R.id.btn_app_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CordovaResourceUpdate.shareUpdate().checkUpdate();
            }
        });
        CordovaResourceUpdate.shareUpdate().setCurrentResourceVersion(0);
        Toast.makeText(this,DeviceUtils.getPhoneName(),Toast.LENGTH_LONG).show();
        DeviceUtils.getSyetemVersion();

        Log.i(TAG, "onCreate: 厂商:"+DeviceUtils.getBrand());
        Log.i(TAG, "onCreate: 型号:"+DeviceUtils.getModel());
        Log.i(TAG, "onCreate: udid:"+DeviceUtils.getUDID(this));
    }
}
