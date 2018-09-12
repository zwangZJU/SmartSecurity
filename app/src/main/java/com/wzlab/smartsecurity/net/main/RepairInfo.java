package com.wzlab.smartsecurity.net.main;

import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wzlab on 2018/8/7.
 */

public class RepairInfo {



    public static void getRepairPreogressInfo(String phone, String repairId, final SuccessCallback successCallback, final FailCallback failCallback){
        new NetConnection(Config.SERVER_URL + Config.ACTION_GRT_REPAIR_PROGRESS, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                successCallback.onSuccess(jsonObject.getString("repair_content"),jsonObject.getString("processing_state"),jsonObject.getString("state_info"));
                            }
                            break;
                        default:
                            if(failCallback!=null){
                                failCallback.onFail(jsonObject.getString(Config.KEY_STATUS));
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(failCallback!=null){
                        failCallback.onFail("数据解析异常");
                    }
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {

            }
        },Config.KEY_PHONE, phone, "repair_id", repairId);

    }


    public static interface SuccessCallback{
        void onSuccess(String content,String processingState, String StateInfo);
    }

    public static interface FailCallback{
        void onFail(String msg);
    }
}
