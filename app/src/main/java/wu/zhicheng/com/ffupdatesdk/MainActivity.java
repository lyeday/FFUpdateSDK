package wu.zhicheng.com.ffupdatesdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zhicheng.ffupdate.CordovaResourceUpdate;
import com.zhicheng.ffupdate.FFUpdate;
import com.zhicheng.ffupdate.utils.UpdateUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdateUtils.BASE_URL = "https://192.168.1.188/apps/";
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
        Toast.makeText(this,"应用启动",Toast.LENGTH_LONG).show();
    }
}
