package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.camera.realplay.RealPlayActivity;
import com.wzlab.smartsecurity.adapter.CameraListAdapter;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Camera;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.utils.DataManager;
import com.wzlab.smartsecurity.utils.DataParser;
import com.wzlab.smartsecurity.utils.EZUtils;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import java.util.ArrayList;

public class DeviceDetailActivity extends AppCompatActivity {


    private static final String TAG = "DeviceDetailActivity";
    public final static int REQUEST_CODE = 100;
    private LoadingLayout loadingLayout;
    private  ArrayList<EZDeviceInfo> mDeviceList = null;
    private ArrayList<EZCameraInfo> mCameraList = null;
    private final static int MSG_LOAD_DEVICE_SUCCESS = 30;
    private EZDeviceInfo deviceInfo;
    private ArrayList<Camera> cameras = null;

   @SuppressLint("HandlerLeak")
   private Handler handler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           if(msg.what == Config.KEY_LOADING_EMPTY){
               loadingLayout.showEmpty();
           }else if(msg.what == Config.KEY_LOADING_ERROR){
               loadingLayout.showError();
               loadingLayout.setRetryListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       initData();
                   }
               });

           }else if(msg.what == Config.KEY_LOADING_SUCCESS){
               loadingLayout.showContent();
           }else if(msg.what == MSG_LOAD_DEVICE_SUCCESS) {


               cameraListAdapter.setCameraList(cameras);

           }else {
               switch (msg.what) {
                   case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
//开启直播

                       Log.e(TAG, "handleMessage: 播放成功" );
                       break;
                   case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                       //播放失败,得到失败信息
                       ErrorInfo errorinfo = (ErrorInfo) msg.obj;
                       //得到播放失败错误码
                       int code = errorinfo.errorCode;
                       //得到播放失败模块错误码
                       String codeStr = errorinfo.moduleCode;
                       //得到播放失败描述
                       String description = errorinfo.description;
                       //得到播放失败解决方方案
                       String sulution = errorinfo.sulution;
                       Log.e(TAG, "出现错误并提出解决方案 "+ errorinfo+code+ codeStr+description+sulution);
                       break;
                   case EZConstants.MSG_VIDEO_SIZE_CHANGED:
                       //解析出视频画面分辨率回调
                       try {
                           String temp = (String) msg.obj;
                           String[] strings = temp.split(":");
                           int mVideoWidth = Integer.parseInt(strings[0]);
                           int mVideoHeight = Integer.parseInt(strings[1]);
                           Log.e(TAG, "handleMessage: "+mVideoWidth+mVideoHeight);
                           //解析出视频分辨率
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                       break;
                   default:
                       break;
               }
           }
       }
   };
    private TextView mTvDeviceId;
    private Switch mSwitchDeviceStatus;
    private ImageView mIvIsAlarming;
    private TextView mTvAddr;
    private TextView mTvPrincipalName;
    private TextView mTvPrincipalPhone;
    private TextView mTvPrincipalPolice;
    private TextView mTvProductionDate;
    private TextView mTvInstallDate;
    private TextView mTvGuaranteeTime;
    private TextView mTvManufacturer;
    private TextView mTvRepairProgress;
    private TextView mTvRepairRecord;
    private TextView mTvDeviceType;
    private String deviceId;
    private RelativeLayout mRvClickToLoadMore;
    private RelativeLayout mRvShowMore;
    private RelativeLayout mRvClickToClose;
    private EZPlayer player;
    private SurfaceHolder surfaceHolder;
    private CameraListAdapter cameraListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setContentView(R.layout.activity_device_detail);
        initView();
        initData();


    }



    @Override
    protected void onStop() {
        super.onStop();
        //停止直播

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();



    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        deviceId = bundle.getString("device_id","11");

        mDeviceList = new ArrayList<>();
        mCameraList = new ArrayList<>();

        GetDeviceInfo.getDeviceDetails(deviceId, new GetDeviceInfo.SuccessCallback() {
            @Override
            public void onSuccess(ArrayList list, final ArrayList camera, String msg) {
                Message handlerMsg = new Message();

                if(camera.size()>0){


                    cameras = camera;
                    if(cameras.get(0).getCamera_serial().length()<5){

                    }else{
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    for(int i=0;i<camera.size();i++) {
                                        Camera c = (Camera)camera.get(i);
                                        deviceInfo = SmartSecurityApplication.getOpenSDK().getDeviceInfo(c.getCamera_serial());
                                        mDeviceList.add(deviceInfo);
                                        EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                                        mCameraList.add(cameraInfo);
                                        cameras.get(i).setCamera_label(cameraInfo.getCameraName());
                                        DataManager.getInstance().setDeviceSerialVerifyCode(c.getCamera_serial(),c.getVerification_code());
                                        EZOpenSDK.getInstance().setAccessToken(c.getAccess_token());
                                    }

                                    Message message = new Message();
                                    message.what = MSG_LOAD_DEVICE_SUCCESS;
                                    handler.sendMessage(message);
                                } catch (BaseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }.start();
                    }


                }

                if(list.size()>0){
                    final Device device = (Device)list.get(0);
                    deviceId = device.getDevice_id();
                    String productType = device.getProduct_type();
                    String status = DataParser.getData(device.getArrange_withdraw(),"0");
                    String isAlarming =  DataParser.getData(device.getIs_alarming(),"0");
                    String productionDate = device.getProduction_date();
                    final String manufacturer = "浙江中创天成科技有限公司";
                    String installDate = device.getInstall_date();
                    String guaranteeTime = device.getGuarantee_time();
                    String userAddr = device.getUser_address().split("#")[0];
                    String repairRecord = device.getRepair_record();
                    String repairProgress = device.getRepair_progress();
                    String head = DataParser.getData(device.getHead(),"暂无数据");
                    String headPhone = DataParser.getData(device.getHead_phone(),"暂无数据");
                    String policeStation = DataParser.getData(device.getPolice_station(),"暂无数据");
                    String deviceType = device.getProduct_type();
                  //  final String cameraSerial = device.getCamera_serial();
                    // TODO 添加验证码
                //    DataManager.getInstance().setDeviceSerialVerifyCode(cameraSerial,"");



                    mTvDeviceId.setText(deviceId);
                   // TODO
                    setSwitchChecked(status);
                    mSwitchDeviceStatus.setEnabled(true);
                    mSwitchDeviceStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String cmd = null;
                            if(mSwitchDeviceStatus.isChecked()){
                                cmd = "5";
                            }else{
                                cmd = "4";
                            }


                            Log.e("ssss", "onClick: " + cmd);
                            GetDeviceInfo.deployDefense(deviceId, cmd, new GetDeviceInfo.SuccessCallbackForDeploy() {
                                @Override
                                public void onSuccess(String deployStatus, String msg) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    setSwitchChecked(deployStatus);
                                }
                            }, new GetDeviceInfo.FailCallback() {
                                @Override
                                public void onFail(String msg) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    if(isAlarming.equals("1")){
                        mIvIsAlarming.setImageDrawable(getDrawable(R.drawable.ic_bottom_nav_bar_alarm_enable));
                    }else {
                        mIvIsAlarming.setImageDrawable(getDrawable(R.drawable.ic_bottom_nav_bar_alarm));
                    }
                    mTvManufacturer.setText("制造商：" + manufacturer);
                    mTvDeviceType.setText("设备型号：" + productType);
                    mTvProductionDate.setText("出厂日期："+productionDate);
                    mTvInstallDate.setText("安装日期："+installDate);
                    mTvGuaranteeTime.setText("保修截止日期："+guaranteeTime);
                    mTvAddr.setText("地址信息："+userAddr);
//                    mTvRepairProgress.setText("报修进度："+repairProgress);
//                    mTvRepairRecord.setText("报修记录："+repairRecord);
                    mTvPrincipalName.setText("姓名："+head);
                    mTvPrincipalPhone.setText("联系电话："+headPhone);
                    mTvPrincipalPolice.setText("所属派出所："+policeStation);


                    handlerMsg.what = Config.KEY_LOADING_SUCCESS;
                    handler.sendMessage(handlerMsg);

                }
            }
        }, new GetDeviceInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message handlerMsg = new Message();
                handlerMsg.what =  Config.KEY_LOADING_ERROR;
                handler.sendMessage(handlerMsg);
                loadingLayout.setErrorText(msg);

                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });



        String deviceStatus = bundle.getString("device_status","1");
//        if(deviceStatus.equals("1")){
//             mSwitchDeviceStatus.setChecked(true);
//        }else {
//            mSwitchDeviceStatus.setChecked(false);
//        }
//        mSwitchDeviceStatus.setEnabled(false);
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.tb_device_detail);
        toolbar.setTitle("设备详情");

        loadingLayout = findViewById(R.id.loading_layout_device_details);
        mTvDeviceId = findViewById(R.id.tv_detail_device_id);
        mTvDeviceId.setText("dssd");
        mSwitchDeviceStatus = findViewById(R.id.switch_detail_device_status);
        mIvIsAlarming = findViewById(R.id.iv_detail_is_alarming);
        mTvAddr = findViewById(R.id.tv_detail_device_location);
        mTvPrincipalName = findViewById(R.id.tv_detail_principal_name);
        mTvPrincipalPhone = findViewById(R.id.tv_detail_principal_phone);
        mTvPrincipalPolice = findViewById(R.id.tv_detail_principal_police);
        mTvDeviceType = findViewById(R.id.tv_detail_device_type);
        mTvProductionDate = findViewById(R.id.tv_detail_production_date);
        mTvInstallDate = findViewById(R.id.tv_detail_install_date);
        mTvGuaranteeTime = findViewById(R.id.tv_detail_guarantee_time);
        mTvManufacturer = findViewById(R.id.tv_detail_manufacturer);
//        mTvRepairProgress = findViewById(R.id.tv_detail_repair_progress);
//        mTvRepairRecord = findViewById(R.id.tv_detail_repair_record);
        mRvClickToLoadMore = findViewById(R.id.rl_load_more);
        mRvShowMore = findViewById(R.id.rl_more);
        mRvClickToClose = findViewById(R.id.rl_close);

        mRvClickToLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRvClickToLoadMore.setVisibility(View.GONE);
                mRvShowMore.setVisibility(View.VISIBLE);

            }
        });

        mRvClickToClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRvClickToLoadMore.setVisibility(View.VISIBLE);
                mRvShowMore.setVisibility(View.GONE);
            }
        });


        RecyclerView mRvCameraInfo = findViewById(R.id.rv_camera_list);
        mRvCameraInfo.setLayoutManager(new LinearLayoutManager(this));
        mCameraList = new ArrayList<>();
        cameras = new ArrayList<>();
        cameraListAdapter = new CameraListAdapter(getApplicationContext(),cameras);
        cameraListAdapter.setOnItemClickListener(new CameraListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(DeviceDetailActivity.this, RealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, mCameraList.get(position));
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, mDeviceList.get(position));
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        mRvCameraInfo.setAdapter(cameraListAdapter);






    }

    public void setSwitchChecked(String status){
        if(status.equals("1")){
            mSwitchDeviceStatus.setChecked(true);
        }else {
            mSwitchDeviceStatus.setChecked(false);
        }
    }


}
