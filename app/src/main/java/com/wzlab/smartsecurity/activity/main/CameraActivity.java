package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;

import java.util.Calendar;

public class CameraActivity extends AppCompatActivity {

    private EZPlayer player;
    private static final String TAG = "CameraActivity";
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
             
                switch (msg.what) {
                    case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
//开启直播

                        Log.e(TAG, "handleMessage: 播放成功" );
                        break;
                    case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                        //播放失败,得到失败信息
                        ErrorInfo errorinfo = (ErrorInfo) msg.obj;
                        //得到播放失败错误码
                        int code = errorinfo.errorCode;
                        //得到播放失败模块错误码
                        String codeStr = errorinfo.moduleCode;
                        //得到播放失败描述
                        String description = errorinfo.description;
                        //得到播放失败解决方方案
                        String sulution = errorinfo.sulution;
                        Log.e(TAG, "出现错误并提出解决方案 "+ errorinfo+code+ codeStr+description+sulution);
                        break;
                    case EZConstants.MSG_VIDEO_SIZE_CHANGED:
                        //解析出视频画面分辨率回调
                        try {
                            String temp = (String) msg.obj;
                            String[] strings = temp.split(":");
                            int mVideoWidth = Integer.parseInt(strings[0]);
                            int mVideoHeight = Integer.parseInt(strings[1]);
                            Log.e(TAG, "handleMessage: "+mVideoWidth+mVideoHeight);
                            //解析出视频分辨率
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
    };
    private EZUIPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
//        SurfaceView surfaceView = findViewById(R.id.sv_show_video);
//        SurfaceHolder surfaceHolder = surfaceView.getHolder();
//        EZOpenSDK.getInstance().setAccessToken("at.2v7tidlg46445ey5any4v9f725ozemnq-3glx455dz5-1atjh0a-hnpbkxcxz");
//        player = EZOpenSDK.getInstance().createPlayer("C26259491",1);
//
//        //设置Handler, 该handler将被用于从播放器向handler传递消息
//        player.setHandler(mHandler);
//
//        //设置播放器的显示Surface
//        player.setSurfaceHold(surfaceHolder);
//
//        /**
//         * 设备加密的需要传入密码
//         * 传入视频加密密码，用于加密视频的解码，该接口可以在收到ERROR_INNER_VERIFYCODE_NEED或ERROR_INNER_VERIFYCODE_ERROR错误回调时调用
//         * @param verifyCode 视频加密密码，默认为设备的6位验证码
//         */
//        player.setPlayVerifyCode("VPAAAO");
//
//        //开启直播
//        player.startRealPlay();

        //获取EZUIPlayer实例
        mPlayer = (EZUIPlayer) findViewById(R.id.player_ui);
Application application = new SmartSecurityApplication();
//初始化EZUIKit
        EZUIKit.initWithAppKey(getApplication(),"7f139f95ccab4ad0be82630b443edb0b");

//设置授权token
        EZUIKit.setAccessToken("at.dxs29glwcjzudt654wrw1i0j84rcolyu-2a2lvrwaw5-1l4j0un-tlhb9qf3a");
//设置播放参数
        mPlayer.setUrl("https://open.ys7.com/view/h5/e69dd707d3614f1cb60a7d07efe32394");
        //创建loadingview
        ProgressBar mLoadView = new ProgressBar(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoadView.setLayoutParams(lp);

//设置loadingview
        mPlayer.setLoadingView(mLoadView);

        //设置播放回调callback
        mPlayer.setCallBack(new EZUIPlayer.EZUIPlayerCallBack() {
            @Override
            public void onPlaySuccess() {

            }

            @Override
            public void onPlayFail(EZUIError ezuiError) {

            }

            @Override
            public void onVideoSizeChange(int i, int i1) {

            }

            @Override
            public void onPrepared() {


                //开始播放
                mPlayer.startPlay();
            }

            @Override
            public void onPlayTime(Calendar calendar) {

            }

            @Override
            public void onPlayFinish() {

            }
        });



    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止播放
        mPlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止直播
//        player.stopRealPlay();
//
//        //释放资源
//        player.release();

        //释放资源
        mPlayer.releasePlayer();
    }
}
