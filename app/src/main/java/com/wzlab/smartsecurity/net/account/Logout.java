package com.wzlab.smartsecurity.net.account;

import com.baidu.location.LocationClient;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wzlab on 2018/7/26.
 */

public class Logout {
    public Logout(String token, final SuccessCallback successCallback, final FailCallback failCallback){

        new NetConnection(Config.SERVER_URL + Config.ACTION_LOGOUT, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                successCallback.onSuccess(jsonObject.getString(Config.RESULT_MESSAGE));
                            }
                            break;
                        default:
                            if(failCallback!=null){
                                failCallback.onFail(jsonObject.getString(Config.RESULT_MESSAGE));
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(failCallback!=null){
                        failCallback.onFail("注销异常");
                    }
                }

            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                if(failCallback!=null){
                    failCallback.onFail("未能连接到服务器");
                }
            }
        });
    }

    public static interface SuccessCallback{
        public void onSuccess(String msg);
    }

    public static interface FailCallback{
        public void onFail(String msg);
    }
}
