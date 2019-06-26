package cn.qssq666.testhook;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int i=0;
    Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            Log.w(AppContext.TAG,"getPackageInfo call");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    i++;
                    Log.w(AppContext.TAG,"getPackageInfo call "+i);
                    handler.postDelayed(this,3000);
                }
            },3000);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.tv)).setText("aaa");

    }
}
