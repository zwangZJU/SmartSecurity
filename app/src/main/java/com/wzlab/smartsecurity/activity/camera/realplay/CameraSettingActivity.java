package com.wzlab.smartsecurity.activity.camera.realplay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.videogo.exception.BaseException;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.main.MainActivity;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.widget.WaitDialog;

public class CameraSettingActivity extends AppCompatActivity {

    private static final String TAG = "CameraSettingActivity";
    private Button mBtnDeleteCamera;
    private String cameraSerial;
    private WaitDialog mWaitDlg;

    private int MSG_DELETE_CAMERA_SUCCESS = 300;
    private int MSG_DELETE_CAMERA_FAIL = 301;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_DELETE_CAMERA_SUCCESS){
                Toast.makeText(getApplicationContext(),"操作成功",Toast.LENGTH_SHORT).show();
                mWaitDlg.dismiss();
                Intent intent = new Intent(CameraSettingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }else if(msg.what == MSG_DELETE_CAMERA_FAIL){
                Toast.makeText(getApplicationContext(),"操作失败",Toast.LENGTH_SHORT).show();
                mWaitDlg.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        setContentView(R.layout.activity_camera_setting);

        Intent intent = getIntent();
        cameraSerial = intent.getStringExtra("camera_serial");
        mBtnDeleteCamera = findViewById(R.id.btn_delete_camera);
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mBtnDeleteCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingActivity.this);
                builder.setMessage("您确定要删除该摄像头吗？")
                        .setPositiveButton("确定!",null)
                        .setNegativeButton("不，打扰了...",null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mWaitDlg.show();
                        deleteCamera(cameraSerial);
                    }
                });

            }
        });
        Log.e(TAG, "onCreate: "+ cameraSerial);
    }

    public void deleteCamera(final String serial){
        new Thread(){

            @Override
            public void run() {
                super.run();

                try {
                    SmartSecurityApplication.getOpenSDK().deleteDevice(serial);
                } catch (BaseException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = MSG_DELETE_CAMERA_FAIL;
                    mHandler.sendMessage(message);
                }
                new NetConnection(Config.SERVER_URL + Config.ACTION_DELETE_CAMERA, HttpMethod.POST, new NetConnection.SuccessCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Message message = new Message();
                        message.what = MSG_DELETE_CAMERA_SUCCESS;
                        mHandler.sendMessage(message);

                    }
                }, new NetConnection.FailCallback() {
                    @Override
                    public void onFail() {
                        Message message = new Message();
                        message.what = MSG_DELETE_CAMERA_FAIL;
                        mHandler.sendMessage(message);
                    }
                });
            }
        }.start();

    }
}
