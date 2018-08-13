package com.wzlab.smartsecurity;

import android.app.Application;
import android.os.Environment;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by wzlab on 2018/8/11.
 */

public class SmartSecurityApplication extends Application {

    private static final String TAG = "SmartSecurityApplicatio";
    @Override
    public void onCreate() {
        super.onCreate();
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
