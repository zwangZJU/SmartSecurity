package com.wzlab.smartsecurity.net.account;

import android.util.Log;
import android.widget.Toast;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.utils.Encrypt;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wzlab on 2018/6/2.
 */

public class Login {
    private static final String TAG = "Login";
    public Login(String phone_md5, final String codeOrPassword, String smsSessionId, final String type, final SuccessCallback successCallback, final FailCallback failCallback){

        //TODO MD5加密后发送
        String passwordOrCode = null;
        String loginBy = null;
        if(type.equals(Config.LOGIN_BY_PASSWORD)){
            loginBy = Config.KEY_PASSWORD;
            passwordOrCode = Encrypt.md5(codeOrPassword);
            Log.e(TAG, passwordOrCode );
        }else {
            passwordOrCode = codeOrPassword;
            loginBy = Config.KEY_SMS_CODE;
        }

        new NetConnection(Config.SERVER_URL + Config.ACTION_LOGIN, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);

                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                successCallback.onSuccess(jsonObject.getString(Config.KEY_TOKEN),jsonObject.getString(Config.RESULT_MESSAGE));
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
                        failCallback.onFail("登录异常");
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
        },Config.KEY_PHONE,phone_md5,Config.KEY_TYPE,type,loginBy, passwordOrCode,Config.KEY_SMS_SESSION_ID,smsSessionId,Config.KEY_ROLE,Config.TYPE_ROLE);
    }

    public static interface SuccessCallback{
        void onSuccess(String token, String msg);
    }

    public static interface FailCallback{
        void onFail(String msg);
    }
}
