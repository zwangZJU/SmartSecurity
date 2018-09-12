package com.wzlab.smartsecurity.activity.main;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.skateboard.zxinglib.CaptureActivity;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.camera.realplay.RealPlayActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiConnectingActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiNetConfigActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.SeriesNumSearchActivity;
import com.wzlab.smartsecurity.adapter.CameraListAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.Camera;
import com.wzlab.smartsecurity.utils.DataManager;
import com.wzlab.smartsecurity.utils.EZUtils;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    private static final int MSG_ADD_CAMERA_SUCCESS = 20;
    private static final int MSG_FAIL_TO_ADD_CAMERA = 21;
    private static final int MSG_EXCEPTION = 22;
    private EZProbeDeviceInfoResult probeResult;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadingLayout loadingLayout;
    private ArrayList<Camera> cameraList;
    private CameraListAdapter cameraListAdapter;
    private String phone;
    private  ArrayList<EZDeviceInfo> mDeviceList = null;
    private ArrayList<EZCameraInfo> mCameraList = null;
    private EZDeviceInfo deviceInfo;
    public final static int REQUEST_CODE = 100;
    private String deviceSerial;
    private String verifyCode;
    private String deviceType;
    private String cameraInfoFromQRCode;
    private SweetAlertDialog mDialog;

    public CameraFragment() {
        // Required empty public constructor
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Config.KEY_LOADING_EMPTY){
                loadingLayout.setOnEmptyButtonClickListener(new LoadingLayout.OnEmptyButtonClickListener() {
                    @Override
                    public void onClick() {
                        cameraBinding();
                    }
                });
                loadingLayout.showEmpty();
            }else if (msg.what == Config.KEY_LOADING_ERROR){
                if(msg.arg1 == 1){
                    loadingLayout.setErrorText("数据解析异常");
                }else {
                    loadingLayout.setErrorText("未能连接服务器");
                }
                loadingLayout.showError();
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadingLayout.showLoading();
                        initData();
                    }
                });
            }else if (msg.what == Config.KEY_LOADING_SUCCESS){
                cameraListAdapter.setCameraList(cameraList);
                loadingLayout.showContent();
            }else if(msg.what == Config.KEY_FINISH_REFRESH){
                swipeRefreshLayout.setRefreshing(false);
            }


            // 添加摄像头
            if (msg.what == MSG_FAIL_TO_ADD_CAMERA){
                switch (msg.arg1){
                    case 120020:
                        // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
                        Toast.makeText(getContext(),"设备在线，已经被自己添加",Toast.LENGTH_LONG).show();
                        break;
                    case 120022:
                        // TODO: 2018/6/25  设备在线，已经被别的用户添加 (给出提示)
                        Toast.makeText(getContext(),"设备在线，已经被别的用户添加",Toast.LENGTH_LONG).show();
                        break;
                    case 120024:
                        // TODO: 2018/6/25  设备不在线，已经被别的用户添加 (给出提示)
                        Toast.makeText(getContext(),"设备不在线，已经被别的用户添加",Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(),"请等待一分钟后再试",Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(),"未能连接服务器",Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        // TODO: 2018/6/25 请求异常

                        Toast.makeText(getContext(),"请求异常",Toast.LENGTH_LONG).show();
                        break;
                }
                mDialog.dismiss();
            }else if(msg.what == MSG_ADD_CAMERA_SUCCESS){

                if(msg.arg1 == 1){
                    Intent intent = new Intent(getContext(),SeriesNumSearchActivity.class);
                    intent.putExtra("deviceSerial",deviceSerial);
                    intent.putExtra("verifyCode",verifyCode);
                    intent.putExtra("deviceType",deviceType);
                    startActivity(intent);
                    mDialog.dismiss();
                }else if(msg.arg1 == 2){
                    DataManager.getInstance().setDeviceSerialVerifyCode(deviceSerial,verifyCode);
                    //  String s =  DataManager.getInstance().getDeviceSerialVerifyCode(deviceSerial);
                    mDialog.dismiss();
                    Intent intent = new Intent(getContext(), AutoWifiConnectingActivity.class);
                    intent.putExtra("success","1");
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO,deviceSerial);
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_VERYCODE,verifyCode);
                    intent.putExtra(AutoWifiNetConfigActivity.DEVICE_TYPE,deviceType);
                    startActivity(intent);
                    //   Toast.makeText(getContext(),"设备添加成功",Toast.LENGTH_LONG).show();

                }

            }else if(msg.what == MSG_EXCEPTION){
                mDialog.dismiss();
                Toast.makeText(getContext(),"请求异常，请长按摄像头复位按钮进行重置",Toast.LENGTH_LONG).show();
            }

        }
    };

   

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        phone = Config.getCachedPhone(getContext());
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        mDialog.setTitleText("正在连接...");
        mDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        mDialog.setCancelable(false);
        
        loadingLayout = view.findViewById(R.id.loading_layout_camera_overview);
        swipeRefreshLayout = view.findViewById(R.id.srl_refresh_camera_list);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(){
                    @Override
                    public void run() {
                        initData();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message();
                        message.what = Config.KEY_FINISH_REFRESH;
                        handler.sendMessage(message);
                    }
                }.start();
            }
        });
        recyclerView = view.findViewById(R.id.rv_camera_overview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cameraList = new ArrayList<>();
        cameraListAdapter = new CameraListAdapter(getContext(), cameraList);
        cameraListAdapter.setOnItemClickListener(new CameraListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), RealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, mCameraList.get(position));
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, mDeviceList.get(position));
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(cameraListAdapter);


    }

    @Override
    public void onStart() {
        super.onStart();
        initData();
    }

    private void initData() {

        mDeviceList = new ArrayList<>();
        mCameraList = new ArrayList<>();
        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_CAMERA_LIST, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                cameraList.clear();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("camera");
                    Gson gson = new Gson();
                    for(int i=0;i<jsonArray.length();i++){
                        cameraList.add(gson.fromJson(jsonArray.get(i).toString(),Camera.class));
                    }

                    if(cameraList.size()>0){
                        getEZCameraList();

                    }else {
                        Message message = new Message();
                        message.what = Config.KEY_LOADING_EMPTY;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = Config.KEY_LOADING_ERROR;
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }


            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what = Config.KEY_LOADING_ERROR;
                message.arg1 = 2;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone, Config.KEY_ROLE, Config.TYPE_ROLE);


    }

    private void getEZCameraList() {
        if(cameraList.get(0).getCamera_serial().length()<5){

        }else{
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        for(int i=0;i<cameraList.size();i++) {
                            Camera c = (Camera)cameraList.get(i);
                            deviceInfo = SmartSecurityApplication.getOpenSDK().getDeviceInfo(c.getCamera_serial());
                            mDeviceList.add(deviceInfo);
                            EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                            mCameraList.add(cameraInfo);
                            cameraList.get(i).setCamera_label(cameraInfo.getCameraName());
                            DataManager.getInstance().setDeviceSerialVerifyCode(c.getCamera_serial(),c.getVerification_code());
                            EZOpenSDK.getInstance().setAccessToken(c.getAccess_token());
                        }
                        Message message = new Message();
                        message.what = Config.KEY_LOADING_SUCCESS;
                        handler.sendMessage(message);
                    } catch (BaseException e) {
                        e.printStackTrace();
                        Message message = new Message();
                        message.what = Config.KEY_LOADING_ERROR;
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }
                }
            }.start();
        }

    }
    private void cameraBinding() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("提示")
                .setMessage("您将通过扫描摄像头上的二维码进行添加,点击“继续”开始扫描")
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent=new Intent(getContext(), CaptureActivity.class);
                        //跳转到扫描二维码页面
                        startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_CAMERA);
                    }
                })
                .setNegativeButton("算了",null).create();
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Config.SCAN_QR_CODE_TO_ADD_CAMERA && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra(CaptureActivity.KEY_DATA);
            cameraInfoFromQRCode = result;
            final String[] cameraInfo = result.split("\\s+");
            //向自己的服务器添加摄像头信息

            deviceSerial = cameraInfo[1];
            verifyCode = cameraInfo[2];
            deviceType = cameraInfo[3];
            mDialog.show();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    probeResult = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial, deviceType);
                    connectCamera(probeResult);
                }
            }.start();

        }
    }

    // 连接设备，看是否需要连接网络
    public void connectCamera(final EZProbeDeviceInfoResult result){
        if (result.getBaseException() == null){

            //查询成功，添加设备
            //  bindingCamera(deviceIdForBindingCamera, cameraInfoFromQRCode);
            try {
                SmartSecurityApplication.getOpenSDK().addDevice(deviceSerial,verifyCode);
                //查询成功，添加设备

                bindingCamera(phone, cameraInfoFromQRCode,2);


//                Message message = new Message();
//                message.what = MSG_ADD_CAMERA_SUCCESS;
//                message.arg1 = 2;
//                handler.sendMessage(message);
            } catch (BaseException e) {
                e.printStackTrace();

                Message message = new Message();
                message.what = MSG_EXCEPTION;
                handler.sendMessage(message);
            }

            return;
        }else{
            switch (result.getBaseException().getErrorCode()){
                case 120023:
                    // TODO: 2018/6/25  设备不在线，未被用户添加 （这里需要网络配置）
                case 120002:
                    // TODO: 2018/6/25  设备不存在，未被用户添加 （这里需要网络配置）
                case 120029:
                    // TODO: 2018/6/25  设备不在线，已经被自己添加 (这里需要网络配置)
                    mDialog.show();


                    bindingCamera(phone, cameraInfoFromQRCode,1);




                    break;
                case 120020:
                    // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
                    //     Toast.makeText(getContext(),"设备在线，已经被自己添加",Toast.LENGTH_SHORT).show();
                case 120022:
                    // TODO: 2018/6/25  设备在线，已经被别的用户添加 (给出提示)
                    //   Toast.makeText(getContext(),"设备在线，已经被别的用户添加",Toast.LENGTH_SHORT).show();
                case 120024:
                    // TODO: 2018/6/25  设备不在线，已经被别的用户添加 (给出提示)
                    //   Toast.makeText(getContext(),"设备不在线，已经被别的用户添加",Toast.LENGTH_SHORT).show();
                default:
                    // TODO: 2018/6/25 请求异常

                    //   Toast.makeText(getContext(),"请求异常",Toast.LENGTH_SHORT).show();
                    new Thread() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = MSG_FAIL_TO_ADD_CAMERA;
                            message.arg1 = probeResult.getBaseException().getErrorCode();
                            handler.sendMessage(message);
                        }
                    }.start();

                    break;
            }
        }
    }

    // 绑定摄像头
    private void bindingCamera(String deviceId, String CameraInfo,final int msg ){
        new NetConnection(Config.SERVER_URL + Config.ACTION_BINDING_CAMERA, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Message message = new Message();
                    message.what = MSG_ADD_CAMERA_SUCCESS;
                    message.arg1 = msg;
                    handler.sendMessage(message);
                    // Toast.makeText(getContext(),jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //
                    Message message = new Message();
                    message.what = MSG_FAIL_TO_ADD_CAMERA;
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what = MSG_FAIL_TO_ADD_CAMERA;
                message.arg1 = 2;
                handler.sendMessage(message);

            }
        },Config.KEY_DEVICE_ID,deviceId, "cameraInfo",CameraInfo);
    }
}
