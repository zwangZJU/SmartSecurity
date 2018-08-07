package com.wzlab.smartsecurity.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.util.List;

/**
 * Created by wzlab on 2018/8/3.
 */

public class AppConfigUtil {
    // 打开应用权限管理页
    public static void settingAppPermission(Context context){
        if(Build.VERSION.SDK_INT < 23){
            return;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+context.getPackageName()));
        context.startActivity(intent);
    }


    public static void settingNotification(Activity activity){


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APPLICATION_SETTINGS");
            intent.putExtra("app_package", activity.getPackageName());
            intent.putExtra("app_uid", activity.getApplicationInfo().uid);
            intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());


            activity.startActivity(intent);
        }else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }

    }
    // 打开应用设置页
    public static void AppDetailsSetting(Context context){
        if(Build.VERSION.SDK_INT < 22){
            Toast.makeText(context,"您不需要设置此项",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+context.getPackageName()));
        context.startActivity(intent);
    }

    public static boolean isAppRunningBackground(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> appProcesses = null;
        if (activityManager != null) {
            appProcesses = activityManager.getRunningAppProcesses();
        }
        if (appProcesses != null) {
            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                        Log.i("后台", appProcess.processName);
                        return true;
                    }else{
                        Log.i("前台", appProcess.processName);
                        return false;
                    }
                }
            }
        }


        return false;
    }
}
