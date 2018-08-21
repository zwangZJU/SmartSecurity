package com.wzlab.smartsecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.videogo.constant.Constant;
import com.videogo.openapi.bean.EZAccessToken;

/**
 * 监听广播
 *
 * @author fangzhihua
 * @data 2013-1-17
 */
public class EzvizBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "EzvizBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constant.OAUTH_SUCCESS_ACTION)) {
//            Log.i(TAG, "onReceive: OAUTH_SUCCESS_ACTION");
//            Intent toIntent = new Intent(context, com.videogo.ui.cameralist.EZCameraListActivity.class);
//            toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            /*******   获取登录成功之后的EZAccessToken对象   *****/
//            EZAccessToken token = com.videogo.EzvizApplication.getOpenSDK().getEZAccessToken();
//            context.startActivity(toIntent);
        }
    }
}
