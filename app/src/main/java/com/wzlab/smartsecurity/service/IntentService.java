package com.wzlab.smartsecurity.service;

/**
 * Created by wzlab on 2018/8/2.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.GTServiceManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.wzlab.smartsecurity.activity.main.MainActivity;
import com.wzlab.smartsecurity.activity.start.StartActivity;
import com.wzlab.smartsecurity.utils.AppConfigUtil;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class IntentService extends GTIntentService {

    public IntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
       Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        //  Log.e(TAG, "onReceiveClientId -> " + "clientid = " + online);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage msg) {
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage msg) {
        boolean isAppRunningBackground = AppConfigUtil.isAppRunningBackground(context);
        if(isAppRunningBackground){
            Intent intent = new Intent(context, StartActivity.class);
            //保证能够跳转
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("isAlarming", true);
            startActivity(intent);
        }else {
            Intent intent = new Intent(context, MainActivity.class);
            //保证能够跳转
            intent.putExtra("isForeground",true);
            intent.putExtra("isAlarming", true);
            startActivity(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        flags = START_STICKY;
        return GTServiceManager.getInstance().onStartCommand(this, intent, flags, startId);
    }
}
