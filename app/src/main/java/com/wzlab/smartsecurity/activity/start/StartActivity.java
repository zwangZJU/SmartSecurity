package com.wzlab.smartsecurity.activity.start;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.AccountActivity;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.main.MainActivity;
import com.wzlab.smartsecurity.net.account.Login;
import com.wzlab.smartsecurity.service.IntentService;

public class StartActivity extends AppCompatActivity {

    private AlphaAnimation alp;
    private RelativeLayout cl;
    private String message = "2";
    private static final String TAG = "StartActivity";
    private boolean isAlarming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化个推的推送服务
        PushManager pushManager = PushManager.getInstance();
        pushManager.initialize(this.getApplicationContext(), com.wzlab.smartsecurity.service.PushService.class);
        // 注册消息接收服务
        pushManager.registerPushIntentService(getApplicationContext(),IntentService.class);
        String CID =  pushManager.getClientid(getApplicationContext());

        Intent intent = getIntent();
        isAlarming = intent.getBooleanExtra("isAlarming",false);

        Log.i(TAG, "onCreate: "+ CID);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        //透明状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_start);
        cl = findViewById(R.id.rl_start);
        //渐变动画
        alp = new AlphaAnimation(1, 1);
        alp.setDuration(2000);
        alp.setRepeatCount(100);
        cl.setAnimation(alp);
        alp.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // 动画开始时就登录
                String token = Config.getCachedToken(getApplicationContext());
                if(!TextUtils.isEmpty(token)){
                    String phone = Config.getCachedPhone(getApplicationContext());
                    String password = Config.getCachedPassword(getApplicationContext());
                    new Login(phone, password, "",Config.LOGIN_BY_PASSWORD, new Login.SuccessCallback() {
                        @Override
                        public void onSuccess(String token, String msg) {
                            message = "1";
                            //  Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                            //   startActivity(new Intent(StartActivity.this, MainActivity.class));
                            //  finish();
                        }
                    }, new Login.FailCallback() {
                        @Override
                        public void onFail(String msg) {
                            message = "0";
                            //   Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                        }
                    });
                }else{
                    message = "0";
                }


                if(ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE}, 1);
                }


            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

                if(ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                    alp.cancel();
                }
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
                if(ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {


                    if(message.equals("1")){
                        Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                        mainIntent.putExtra("isAlarming",isAlarming);
                        startActivity(mainIntent);



                    }else{
                        startActivity(new Intent(StartActivity.this, AccountActivity.class));
                    }


                }
                finish();

            }
        });
    }

}
