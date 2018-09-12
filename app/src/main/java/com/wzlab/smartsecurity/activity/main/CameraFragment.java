package com.wzlab.smartsecurity.activity.main;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.camera.realplay.RealPlayActivity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {


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

    public CameraFragment() {
        // Required empty public constructor
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Config.KEY_LOADING_EMPTY){
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


}
