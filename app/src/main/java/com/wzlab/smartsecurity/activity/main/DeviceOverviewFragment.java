package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skateboard.zxinglib.CaptureActivity;

import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.start.StartActivity;
import com.wzlab.smartsecurity.adapter.DeviceOverviewAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.utils.CheckNetworkStatus;
import com.wzlab.smartsecurity.widget.LoadingLayout;
import com.wzlab.smartsecurity.widget.WaitDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class DeviceOverviewFragment extends Fragment {

    private static final String TAG = "DeviceOverviewFragment";
    private static final int MSG_ADD_CAMERA_SUCCESS = 20;
    private static final int KEY_FINISH_REFRESH = 3;
    private RecyclerView mRvDeviceOverview;
    private ArrayList<Device> deviceList;
    private DeviceOverviewAdapter deviceOverviewAdapter;
    private LoadingLayout loadingLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String deviceIdForBindingCamera;
    private WaitDialog mWaitDlg;

    public DeviceOverviewFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWaitDlg = new WaitDialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);

    }





    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                        Intent intent=new Intent(getContext(), CaptureActivity.class);
//                            //跳转到扫描二维码页面
                            startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_DEVICE);
                    }
                });


                mRvDeviceOverview.setAdapter(deviceOverviewAdapter);


                loadingLayout.showContent();
            }else if(msg.what == Config.KEY_LOADING_LOADING){
                loadingLayout.showLoading();
            }else if(msg.what == KEY_FINISH_REFRESH){

                swipeRefreshLayout.setRefreshing(false);

            }else if(msg.what == MSG_ADD_CAMERA_SUCCESS){
                mWaitDlg.dismiss();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_device_overview, container, false);

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
            intent.putExtra("deviceInfo", deviceInfo);
            startActivity(intent);
            getActivity().finish();

        }else if(requestCode==Config.SCAN_QR_CODE_TO_ADD_CAMERA && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra(CaptureActivity.KEY_DATA);
            //bindingCamera(deviceIdForBindingCamera, cameraInfo);
            final String[] cameraInfo = result.split("\\s+");
            final String deviceSerial = cameraInfo[1];
            final String deviceType = cameraInfo[3];
            mWaitDlg.show();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    EZProbeDeviceInfoResult probeResult = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial,deviceType);
                    connectCamera(probeResult);
                }
            }.start();

        }
    }

    private void bindingCamera(String deviceId, String CameraInfo) {
        new NetConnection(Config.SERVER_URL + Config.ACTION_BINDING_CAMERA, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Toast.makeText(getContext(),jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"数据解析异常",Toast.LENGTH_SHORT).show();
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getContext(),"未能连接服务器",Toast.LENGTH_SHORT).show();
            }
        },deviceId, CameraInfo);
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
            public void onSuccess(ArrayList list, String msg) {
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
        Log.e(TAG, "onStart: fragmet" );
        initData(false);
    }


    public void connectCamera(final EZProbeDeviceInfoResult result){
        if (result.getBaseException() == null){
            Log.e(TAG, "connectCamera: 查询成功" );
            //查询成功，添加设备

            try {
                SmartSecurityApplication.getOpenSDK().addDevice("C26259491","VPAAAO");
                Message message = new Message();
                message.what = MSG_ADD_CAMERA_SUCCESS;
                handler.sendMessage(message);
            } catch (BaseException e) {
                e.printStackTrace();
                Log.e(TAG, "connectCamera: ",e );
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
                    new Thread() {
                        @Override
                        public void run() {
                            if (result.getEZProbeDeviceInfo() == null){
                                // 未查询到设备信息，不确定设备支持的配网能力,需要用户根据指示灯判断
                                //若设备指示灯红蓝闪烁，请选择smartconfig配网
                                //若设备指示灯蓝色闪烁，请选择设备热点配网
                            }else{
                                // 查询到设备信息，根据能力级选择配网方式
                                if (result.getEZProbeDeviceInfo().getSupportAP() == 2) {
                                    //选择设备热单配网
                                    Log.e(TAG, "connectCamera: 选择设备热单配网" );
                                }
                                if (result.getEZProbeDeviceInfo().getSupportWifi() == 3) {
                                    //选择smartconfig配网
                                    Log.e(TAG, "connectCamera: 选择smartconfig配网" );
                                }
                                if (result.getEZProbeDeviceInfo().getSupportSoundWave() == 1) {
                                    //选择声波配网
                                    Log.e(TAG, "connectCamera: 选择声波配网" );
                                }
                            }
                        }
                    }.start();
                    break;
                case 120020:
                    // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
                case 120022:
                    // TODO: 2018/6/25  设备在线，已经被别的用户添加 (给出提示)
                case 120024:
                    // TODO: 2018/6/25  设备不在线，已经被别的用户添加 (给出提示)
                default:
                    // TODO: 2018/6/25 请求异常
                    Log.e(TAG, "connectCamera: "+ result.getBaseException().getErrorCode() );

                    break;
            }
        }
    }

}
