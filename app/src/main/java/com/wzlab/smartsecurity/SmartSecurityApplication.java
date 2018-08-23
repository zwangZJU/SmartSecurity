package com.wzlab.smartsecurity;

import android.app.Application;
import android.os.Environment;

import android.util.Log;

import com.videogo.openapi.EZOpenSDK;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by wzlab on 2018/8/11.
 */

public class SmartSecurityApplication extends Application {

    private static final String TAG = "SmartSecurityApplicatio";
    //开发者需要填入自己申请的appkey
    public static String AppKey = "7f139f95ccab4ad0be82630b443edb0b";

    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 萤石云sdk初始化
        /** * sdk日志开关，正式发布需要去掉 */
        EZOpenSDK.showSDKLog(true);
        /** * 设置是否支持P2P取流,详见api */
        EZOpenSDK.enableP2P(false);
        /** * APP_KEY请替换成自己申请的 */
        EZOpenSDK.initLib(this, AppKey);




        // 捕获全局异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                String path = getExternalFilesDir(null).getPath();
                fileWriter(path);
                System.exit(0);


            }
        });
    }

    private void fileWriter(String dir){
        String fn = dir + "/wzlab.txt";
       // mTvShowResult.setText(mTvShowResult.getText() + " \nWrite to: "+fn);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fn)));
            pw.println(fn);
            pw.close();
        } catch (IOException e) {
          //  mTvShowResult.setText(mTvShowResult.getText()+ "\nWrite to"+ e.toString());
            e.printStackTrace();
            Log.e(TAG, "fileWriter: ",e );
        }

    }
}
