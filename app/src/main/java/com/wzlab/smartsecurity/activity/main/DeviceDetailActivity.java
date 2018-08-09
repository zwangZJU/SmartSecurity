package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.widget.ColorfulProgressBar;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import java.util.ArrayList;

public class DeviceDetailActivity extends AppCompatActivity {

    private static final int KEY_LOADING_ERROR = -1;
    private static final int KEY_LOADING_EMPTY = 0;
    private static final int KEY_LOADING_SUCCESS = 1;
    private static final int KEY_LOADING_LOADING = 2;

    private LoadingLayout loadingLayout;

   @SuppressLint("HandlerLeak")
   private Handler handler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           if(msg.what == KEY_LOADING_EMPTY){
               loadingLayout.showEmpty();
           }else if(msg.what == KEY_LOADING_ERROR){
               loadingLayout.showError();
               loadingLayout.setRetryListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       initData();
                   }
               });

           }else if(msg.what == KEY_LOADING_SUCCESS){
               loadingLayout.showContent();
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

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        deviceId = bundle.getString("device_id","11");
        GetDeviceInfo.getDeviceDetails(deviceId, new GetDeviceInfo.SuccessCallback() {
            @Override
            public void onSuccess(ArrayList list, String msg) {
                Message handlerMsg = new Message();
                if(list.size()>0){
                    Device device = (Device)list.get(0);
                    deviceId = getData(device.getDevice_id());
                    String productType = getData(device.getProduct_type());
                    String status = getData(device.getArrange_withdraw());
                    String isAlarming = getData(device.getIs_alarming());
                    String productionDate = getData(device.getProduction_date());
                    String manufacturer = "浙江中创天成科技有限公司";
                    String installDate = getData(device.getInstall_date());
                    String guaranteeTime = getData(device.getGuarantee_time());
                    String userAddr = getData(device.getUser_address().split("#")[0]);
                    String repairRecord = getData(device.getRepair_record());
                    String repairProgress = getData(device.getRepair_progress());
                    String head = getData(device.getHead());
                    String headPhone = getData(device.getHead_phone());
                    String policeStation = getData(device.getPolice_station());
                    String deviceType = getData(device.getProduct_type());

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
                    mTvRepairProgress.setText("报修进度："+repairProgress);
                    mTvRepairRecord.setText("报修记录："+repairRecord);
                    mTvPrincipalName.setText("姓名："+head);
                    mTvPrincipalPhone.setText("联系电话："+headPhone);
                    mTvPrincipalPolice.setText("所属派出所："+policeStation);


                    handlerMsg.what = KEY_LOADING_SUCCESS;
                    handler.sendMessage(handlerMsg);

                }
            }
        }, new GetDeviceInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message handlerMsg = new Message();
                handlerMsg.what =  KEY_LOADING_ERROR;
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
        mTvRepairProgress = findViewById(R.id.tv_detail_repair_progress);
        mTvRepairRecord = findViewById(R.id.tv_detail_repair_record);






    }

    public void setSwitchChecked(String status){
        if(status.equals("1")){
            mSwitchDeviceStatus.setChecked(true);
        }else {
            mSwitchDeviceStatus.setChecked(false);
        }
    }

    public String getData(String s){
        if(s == null){
            return "暂无数据";
        }else {
            return s;
        }
    }
}
