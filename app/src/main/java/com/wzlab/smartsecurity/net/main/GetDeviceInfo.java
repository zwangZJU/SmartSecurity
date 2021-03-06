package com.wzlab.smartsecurity.net.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.net.account.GetSmsCode;
import com.wzlab.smartsecurity.po.Camera;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.utils.DataParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by wzlab on 2018/7/11.
 */

public class GetDeviceInfo {
    private static final String TAG = "GetDeviceInfo";
    Context context;
    int layoutResource;
    int containerId;

    public GetDeviceInfo(Context context, int layoutResource, int containerId){
        this.context = context;
        this.layoutResource = layoutResource;
        this.containerId = containerId;
    }



    public static interface SuccessCallback{
        void onSuccess(ArrayList list, ArrayList camera, String msg);
    }
    public static interface SuccessCallbackForDeploy{
        void onSuccess(String cmd, String msg);
    }

    public static interface FailCallback{
        void onFail(String msg);
    }


    // 布防撤防
    public static void deployDefense(String deviceId, String cmd, final SuccessCallbackForDeploy successCallback, final FailCallback failCallback){
        String url = Config.SERVER_URL + Config.ACTION_DEPLOY_DEFENSE;
        new NetConnection(url, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                successCallback.onSuccess(DataParser.getData(jsonObject.getString("deploy_status"),"0"),jsonObject.getString(Config.RESULT_MESSAGE));
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
                        failCallback.onFail("数据解析异常");
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
        },Config.KEY_DEVICE_ID, deviceId, Config.KEY_COMMAND, cmd);
    }

    // 获取设备列表
    public static void getDeviceList(String phone, String role, final SuccessCallback successCallback, final FailCallback failCallback){
        String url = Config.SERVER_URL + Config.ACTION_GET_DEVICE_LIST;
        new NetConnection(url, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                //TODO
                                ArrayList<Device> deviceList = new ArrayList<Device>();
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                Gson gson = new Gson();
                                for(int i=0;i<jsonArray.length();i++){
                                    deviceList.add(gson.fromJson(jsonArray.get(i).toString(),Device.class));
                                }
                                successCallback.onSuccess(deviceList,null,jsonObject.getString(Config.RESULT_MESSAGE));
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
                        failCallback.onFail("数据解析异常");
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
        },Config.KEY_PHONE, phone, Config.KEY_ROLE, role);
    }

    // 设备绑定
    public static void deviceBinding(String phone, String locLabel, String role, String deviceInfo, String addrInfo, String deviceType, final SuccessCallback successCallback, final FailCallback failCallback){
        String url = Config.SERVER_URL + "deviceBinding";
        new NetConnection(url, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:

                                successCallback.onSuccess(null,null,jsonObject.getString(Config.RESULT_MESSAGE));
//                            }
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
                        failCallback.onFail("解析异常");
                    }
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                 if(failCallback != null){
                     failCallback.onFail("请重试");
                 }
            }
        },Config.KEY_PHONE, phone, Config.KEY_LOCATION_LABEL, locLabel, Config.KEY_ROLE, role, "deviceInfo", deviceInfo, "address", addrInfo, "deviceType", deviceType);
    }


    // 获得设备详细数据
    @SuppressLint("StaticFieldLeak")
    public static void getDeviceDetails(String deviceId, final SuccessCallback successCallback, final FailCallback failCallback){
        ArrayList list = new ArrayList();

        new NetConnection(Config.SERVER_URL+Config.ACTION_GET_DEVICE_DETAILS, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            if(successCallback!=null){
                                ArrayList<Device> deviceList = new ArrayList<>();


                                // 得到报警模块的基本信息
                                JSONObject jsonArray = jsonObject.getJSONObject("data");
                                Gson gson = new Gson();
                                deviceList.add(gson.fromJson(jsonArray.toString(),Device.class));
                                ArrayList<Camera> cameraList = new ArrayList<>();
                            //   JSONObject jsonCameraObject = jsonObject.getJSONObject("camera");
                                JSONArray jsonCameraArray = jsonObject.getJSONArray("camera");

                                for(int i=0;i<jsonCameraArray.length();i++){
                                    cameraList.add(gson.fromJson(jsonCameraArray.get(i).toString(),Camera.class));
                                }
                                successCallback.onSuccess(deviceList,cameraList,jsonObject.getString(Config.RESULT_MESSAGE));
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
                        failCallback.onFail("数据解析异常");
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
        },"device_id",deviceId);

    }
}
