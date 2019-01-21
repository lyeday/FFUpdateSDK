package wu.zhicheng.com.ffupdatesdk;

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

//./gradlew clean build bintrayUpload -PbintrayUser=voisen -PbintrayKey=5e1327b3fc386d03fb328e4c75e2270eef2962cc -PdryRun=false
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdateUtils.BASE_URL = "https://192.168.1.188/apps/";
        SPUtils.init(this).setAppVersion(1).setAppReadyVersion(1).save();
        FFUpdate.shareUpdate().registerAppKey("QNLACVZPMviFLTkcsy1GoGcMrPiz4BTP",getApplication());
        FFUpdate.shareUpdate().checkUpdate();
        CordovaResourceUpdate.shareUpdate().registerKey("QNLACVZPMviFLTkcsy1GoGcMrPiz4BTP",getApplication());
        CordovaResourceUpdate.shareUpdate().setCurrentResourceVersion(1);
        findViewById(R.id.btn_app_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CordovaResourceUpdate.shareUpdate().checkUpdate();
            }
        });

        Toast.makeText(this,DeviceUtils.getPhoneName(),Toast.LENGTH_LONG).show();
        DeviceUtils.getSyetemVersion();

        Log.i(TAG, "onCreate: 唯一标识:"+DeviceUtils.getUDID(this));
    }
}
