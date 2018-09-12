package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.skateboard.zxinglib.CaptureActivity;

import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiConnectingActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiNetConfigActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.SeriesNumSearchActivity;
import com.wzlab.smartsecurity.adapter.DeviceOverviewAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.utils.DataManager;
import com.wzlab.smartsecurity.widget.CustomPopWindow;
import com.wzlab.smartsecurity.widget.LoadingLayout;
import com.wzlab.smartsecurity.widget.WaitDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.videogo.util.Utils.showToast;


public class IntelligentTerminalFragment extends Fragment {




    private static final String TAG = "IntelligentTerminalFragment";
    private static final int MSG_ADD_CAMERA_SUCCESS = 20;
    private static final int MSG_FAIL_TO_ADD_CAMERA = 21;
    private static final int MSG_EXCEPTION = 22;
    private static final int KEY_FINISH_REFRESH = 3;
    private RecyclerView mRvDeviceOverview;
    private ArrayList<Device> deviceList;
    private DeviceOverviewAdapter deviceOverviewAdapter;
    private LoadingLayout loadingLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String deviceIdForBindingCamera;
    private WaitDialog mWaitDlg;
    private EZProbeDeviceInfoResult probeResult;
    private String cameraInfoFromQRCode;

    private String deviceSerial;
    private String deviceType;
    private String verifyCode;

    private CustomPopWindow mCustomPopWindow;
    private String deviceTypeForBind = "0";
    private String phone;

    public IntelligentTerminalFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phone = Config.getCachedPhone(getContext());
        mWaitDlg = new WaitDialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);

    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                mWaitDlg.dismiss();
            }else if(msg.what == MSG_ADD_CAMERA_SUCCESS){

                if(msg.arg1 == 1){
                    Intent intent = new Intent(getContext(),SeriesNumSearchActivity.class);
                    intent.putExtra("deviceSerial",deviceSerial);
                    intent.putExtra("verifyCode",verifyCode);
                    intent.putExtra("deviceType",deviceType);
                    startActivity(intent);
                    mWaitDlg.dismiss();
                }else if(msg.arg1 == 2){
                    DataManager.getInstance().setDeviceSerialVerifyCode(deviceSerial,verifyCode);
                    //  String s =  DataManager.getInstance().getDeviceSerialVerifyCode(deviceSerial);
                    mWaitDlg.dismiss();
                    Intent intent = new Intent(getContext(), AutoWifiConnectingActivity.class);
                    intent.putExtra("success","1");
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO,deviceSerial);
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_VERYCODE,verifyCode);
                    intent.putExtra(AutoWifiNetConfigActivity.DEVICE_TYPE,deviceType);
                    startActivity(intent);
                 //   Toast.makeText(getContext(),"设备添加成功",Toast.LENGTH_LONG).show();

                }

            }else if(msg.what == MSG_EXCEPTION){
                mWaitDlg.dismiss();
                Toast.makeText(getContext(),"请求异常，请长按摄像头复位按钮进行重置",Toast.LENGTH_LONG).show();
            }


            if(msg.what == Config.KEY_LOADING_EMPTY){
                loadingLayout.showEmpty();
                //空白页的添加按钮
                loadingLayout.setOnEmptyButtonClickListener(new LoadingLayout.OnEmptyButtonClickListener() {
                    @Override
                    public void onClick() {
                      //  Toast.makeText(getContext(),"ddd",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getContext(), CaptureActivity.class);
                        //跳转到扫描二维码页面
                        startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);

                    }
                });
            }else if(msg.what == Config.KEY_LOADING_ERROR){
                loadingLayout.showError();
            }else if(msg.what == Config.KEY_LOADING_SUCCESS){
                deviceOverviewAdapter = new DeviceOverviewAdapter(getContext(),deviceList);
                // TODO 暂时取消item的点击事件
//                deviceOverviewAdapter.setOnItemClickListener(new DeviceOverviewAdapter.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        if(position<deviceList.size()){
//                            Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
//                            Device device = deviceList.get(position);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("device_id",device.getDevice_id());
//
//
//                            intent.putExtras(bundle);
//                            startActivity(intent);
//                        }else{
//                            Intent intent=new Intent(getContext(), CaptureActivity.class);
//                            //跳转到扫描二维码页面
//                            startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
//                        }
//
//
//                        // getFragmentManager().beginTransaction().addToBackStack(null).add(deviceDetailFragment,)
//                    }
//                });

                // 设置switch的点击事件
                deviceOverviewAdapter.setOnSwitchClickListener(new DeviceOverviewAdapter.OnSwitchClickListener() {
                    @Override
                    public void onSwitchClick(View view, int position) {
                      //  Toast.makeText(getContext(),"dd",Toast.LENGTH_SHORT).show();

//                        initData(true);
//                        deviceOverviewAdapter.setDeviceList(deviceList);
                    }
                });
                // 设置Icon的点击事件,跳转到设备详情页
                deviceOverviewAdapter.setOnIconClickListener(new DeviceOverviewAdapter.OnIconClickListener() {
                    @Override
                    public void onIconClick(View view, int position) {
                        if(position<deviceList.size()){
                            Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                            Device device = deviceList.get(position);
                            Bundle bundle = new Bundle();
                            bundle.putString("device_id",device.getDevice_id());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else{
                            Intent intent=new Intent(getContext(), CaptureActivity.class);
                            //跳转到扫描二维码页面
                            startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
                        }


                        // getFragmentManager().beginTransaction().addToBackStack(null).add(deviceDetailFragment,)
                    }

                });

                // 添加摄像头的点击事件
                deviceOverviewAdapter.setOnLinearLayoutClickListener(new DeviceOverviewAdapter.OnLinearLayoutClickListener() {
                    @Override
                    public void onLinearLayoutClick(View view, final int position) {
                        // TODO 添加摄像头
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setTitle("提示")
                                .setMessage("您将通过扫描摄像头上的二维码进行添加,点击“继续”开始扫描")
                                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deviceIdForBindingCamera = deviceList.get(position).getDevice_id();
                                        Intent intent=new Intent(getContext(), CaptureActivity.class);
                                        //跳转到扫描二维码页面
                                        startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_CAMERA);
                                    }
                                })
                                .setNegativeButton("算了",null).create();
                        alertDialog.show();
                    }
                });

                // 添加设备的点击事件
                deviceOverviewAdapter.setOnFloatingActionButtonClickListener(new DeviceOverviewAdapter.OnFloatingActionButtonClickListener() {
                    @Override
                    public void onFloatingActionButtonClick(View view, int position) {
                        showPopMenu();
//                        Intent intent=new Intent(getContext(), CaptureActivity.class);
////                            //跳转到扫描二维码页面
//                            startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
                    }
                });


                mRvDeviceOverview.setAdapter(deviceOverviewAdapter);


                loadingLayout.showContent();
            }else if(msg.what == Config.KEY_LOADING_LOADING){
                loadingLayout.showLoading();
            }else if(msg.what == KEY_FINISH_REFRESH){

                swipeRefreshLayout.setRefreshing(false);

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_intelligent_terminal, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mRvDeviceOverview = rootView.findViewById(R.id.rv_device_overview);
        mRvDeviceOverview.setLayoutManager(new GridLayoutManager(getContext(),2));

        loadingLayout = rootView.findViewById(R.id.loading_layout_device_overview);
        initData(false);
        //没有网络 页面 的重新加载
        loadingLayout.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initData(false);
            }
        });




        swipeRefreshLayout = rootView.findViewById(R.id.srl_refresh_device_list);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        initData(true);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message();
                        message.what = KEY_FINISH_REFRESH;
                        handler.sendMessage(message);
                    }
                }.start();
            }
        });





    }

    // 扫完二维码返回到这里
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Config.SCAN_QR_CODE_TO_ADD_DEVICE && resultCode== Activity.RESULT_OK)
        {
            String phone = Config.getCachedPhone(getContext());
            String deviceInfo=data.getStringExtra(CaptureActivity.KEY_DATA);
            Intent intent = new Intent(getContext(),SelectLocationActivity.class);
            if(deviceInfo.length()>10 && deviceInfo.substring(8,9).equals("#")){
                intent.putExtra("deviceInfo", deviceInfo);
                intent.putExtra("deviceType",deviceTypeForBind);
                startActivity(intent);
                getActivity().finish();
            }else{
                Toast.makeText(getContext().getApplicationContext(),"无效二维码",Toast.LENGTH_LONG).show();
            }


        }else if(requestCode==Config.SCAN_QR_CODE_TO_ADD_CAMERA && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra(CaptureActivity.KEY_DATA);
            cameraInfoFromQRCode = result;
            final String[] cameraInfo = result.split("\\s+");
            //向自己的服务器添加摄像头信息

            deviceSerial = cameraInfo[1];
            verifyCode = cameraInfo[2];
            deviceType = cameraInfo[3];
            mWaitDlg.show();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    probeResult = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial,deviceType);
                    connectCamera(probeResult);
                }
            }.start();

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

    public void initData(boolean isPulling) {

        deviceList = new ArrayList<Device>();

        //显示加载页面，并加载数据
        if(!isPulling){
            loadingLayout.showLoading();
        }


        String phone = Config.getCachedPhone(getContext());

        GetDeviceInfo.getDeviceList(phone, Config.TYPE_ROLE, new GetDeviceInfo.SuccessCallback() {
            @Override
            public void onSuccess(ArrayList list, ArrayList camera, String msg) {
                Message handlerMsg = new Message();
                if(list.size()>0){
                    deviceList = list;
                    handlerMsg.what = Config.KEY_LOADING_SUCCESS;
                }else{//如果为空
                    handlerMsg.what = Config.KEY_LOADING_EMPTY;
                }
                handler.sendMessage(handlerMsg);
            }
        }, new GetDeviceInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message handlerMsg = new Message();
                handlerMsg.what = Config.KEY_LOADING_ERROR;
                handler.sendMessage(handlerMsg);
                loadingLayout.setErrorText(msg);
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
      //  deviceOverviewAdapter = new DeviceOverviewAdapter(getContext(),deviceList);
    }

    @Override
    public void onStart() {
        super.onStart();

        initData(false);
    }


    // 连接设备，看是否需要连接网络
    public void connectCamera(final EZProbeDeviceInfoResult result){
        if (result.getBaseException() == null){

            //查询成功，添加设备
          //  bindingCamera(deviceIdForBindingCamera, cameraInfoFromQRCode);
            try {
                SmartSecurityApplication.getOpenSDK().addDevice(deviceSerial,verifyCode);
                //查询成功，添加设备
                if(deviceTypeForBind.equals("3")){
                    bindingCamera(phone, cameraInfoFromQRCode,2);
                }else{
                    bindingCamera(deviceIdForBindingCamera, cameraInfoFromQRCode,2);
                }

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
                    mWaitDlg.show();

                    if(deviceTypeForBind.equals("3")){
                        bindingCamera(phone, cameraInfoFromQRCode,1);
                    }else{
                        bindingCamera(deviceIdForBindingCamera, cameraInfoFromQRCode,1);
                    }



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



    private void showPopMenu(){
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.content_pop_menu,null);
        Button mBtnAddAlarmModel = contentView.findViewById(R.id.btn_add_alarm_model);
        Button mBtnAddOneButtonDevice = contentView.findViewById(R.id.btn_add_one_button_device);
        Button mBtnAddCamera = contentView.findViewById(R.id.btn_add_camera);
        contentView.findViewById(R.id.view_may_be_gone).setVisibility(View.GONE);
        mBtnAddCamera.setVisibility(View.GONE);
        mBtnAddAlarmModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "1";
                Intent intent=new Intent(getContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
                mCustomPopWindow.dissmiss();
            }
        });
        mBtnAddOneButtonDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "2";
                Intent intent=new Intent(getContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
                mCustomPopWindow.dissmiss();
            }
        });
        mBtnAddCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "3";
                Intent intent=new Intent(getContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_CAMERA );
                mCustomPopWindow.dissmiss();
            }
        });

        mCustomPopWindow=new CustomPopWindow.PopupWindowBuilder(getActivity())
                .setView(contentView)
                .enableBackgroundDark(true)
                .create()
                .showAtLocation(getActivity().findViewById(R.id.fl_main_container), Gravity.BOTTOM,0,0);
    }



}
