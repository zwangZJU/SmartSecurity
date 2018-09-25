package com.wzlab.smartsecurity.activity.main;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.skateboard.zxinglib.CaptureActivity;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.SmartSecurityApplication;
import com.wzlab.smartsecurity.activity.account.AccountActivity;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.camera.playback.PlayBackListActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiConnectingActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.AutoWifiNetConfigActivity;
import com.wzlab.smartsecurity.activity.camera.wifi.SeriesNumSearchActivity;
import com.wzlab.smartsecurity.activity.me.FeedBackActivity;
import com.wzlab.smartsecurity.activity.me.MeFragment;
import com.wzlab.smartsecurity.activity.me.PersonalCenterActivity;
import com.wzlab.smartsecurity.activity.me.PersonalCenterFragment;
import com.wzlab.smartsecurity.activity.message.AdvertisementFragment;
import com.wzlab.smartsecurity.activity.repair.DeviceFaultReportFragment;
import com.wzlab.smartsecurity.adapter.ViewPagerAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.net.account.Logout;
import com.wzlab.smartsecurity.utils.AppConfigUtil;
import com.wzlab.smartsecurity.utils.DataManager;
import com.wzlab.smartsecurity.utils.DataParser;
import com.wzlab.smartsecurity.utils.GraphProcess;
import com.wzlab.smartsecurity.widget.BottomNavMenuBar;
import com.wzlab.smartsecurity.widget.CustomPopWindow;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;
import com.wzlab.smartsecurity.widget.WaitDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MSG_ADD_CAMERA_SUCCESS = 20;
    private static final int MSG_FAIL_TO_ADD_CAMERA = 21;
    private static final int MSG_EXCEPTION = 22;

    private NoScrollViewPager mVpMainContainer;
    private Toolbar toolbar;
    private String phone;
    private static final String TAG = "MainActivity";
    private BottomNavMenuBar mBottomNavMenuBar;
    private FrameLayout mFlMainContainer;
    //v4包下的FragmentManager
    public static android.support.v4.app.FragmentManager sV4FragManager ;
    private String cameraInfoFromQRCode;
    private EZProbeDeviceInfoResult probeResult;
    private String deviceSerial;
    private WaitDialog mWaitDlg;
    private String verifyCode;

    private Menu mMenu;
    private DrawerLayout drawer;
    private String deviceType;
    private String userInfoName = "";
    private String userInfoAvatarURL = null;
    private String userInfoIsCert = "";
    private Bitmap bmAvatar;
    private static int LOAD_USER_INFO_TEXT_SUCCESS = 5;
    private static int LOAD_USER_INFO_ALL_SUCCESS = 6;
    private String deviceTypeForBind;
    private boolean isExit = false; //按两次退出
    private static int ACTION_EXIT = -1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //按两次退出
            if (msg.what == ACTION_EXIT){
                isExit = false;
            }


            if(msg.what == LOAD_USER_INFO_TEXT_SUCCESS){
                mTvUserInfoName.setText(userInfoName);
                mTvUserInfoIsCert.setText(userInfoIsCert);
            }else if(msg.what == LOAD_USER_INFO_ALL_SUCCESS){
                mTvUserInfoName.setText(userInfoName);
                mTvUserInfoIsCert.setText(userInfoIsCert);
                mIvNavAvatar.setImageBitmap(bmAvatar);
            }

            // 添加摄像头
            if (msg.what == MSG_FAIL_TO_ADD_CAMERA){
                switch (msg.arg1){
                    case 120020:
                        // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
                        Toast.makeText(getApplicationContext(),"设备在线，已经被自己添加",Toast.LENGTH_LONG).show();
                        break;
                    case 120022:
                        // TODO: 2018/6/25  设备在线，已经被别的用户添加 (给出提示)
                        Toast.makeText(getApplicationContext(),"设备在线，已经被别的用户添加",Toast.LENGTH_LONG).show();
                        break;
                    case 120024:
                        // TODO: 2018/6/25  设备不在线，已经被别的用户添加 (给出提示)
                        Toast.makeText(getApplicationContext(),"设备不在线，已经被别的用户添加",Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(),"请等待一分钟后再试",Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(),"未能连接服务器",Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        // TODO: 2018/6/25 请求异常

                        Toast.makeText(getApplicationContext(),"请求异常",Toast.LENGTH_LONG).show();
                        break;
                }
                mWaitDlg.dismiss();
            }else if(msg.what == MSG_ADD_CAMERA_SUCCESS){

                if(msg.arg1 == 1){
                    Intent intent = new Intent(getApplicationContext(),SeriesNumSearchActivity.class);
                    intent.putExtra("deviceSerial",deviceSerial);
                    intent.putExtra("verifyCode",verifyCode);
                    intent.putExtra("deviceType",deviceType);
                    startActivity(intent);
                    mWaitDlg.dismiss();
                }else if(msg.arg1 == 2){
                    DataManager.getInstance().setDeviceSerialVerifyCode(deviceSerial,verifyCode);
                    //  String s =  DataManager.getInstance().getDeviceSerialVerifyCode(deviceSerial);
                    mWaitDlg.dismiss();
                    Intent intent = new Intent(getApplicationContext(), AutoWifiConnectingActivity.class);
                    intent.putExtra("success","1");
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO,deviceSerial);
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_VERYCODE,verifyCode);
                    intent.putExtra(AutoWifiNetConfigActivity.DEVICE_TYPE,deviceType);
                    startActivity(intent);
                    //   Toast.makeText(getContext(),"设备添加成功",Toast.LENGTH_LONG).show();

                }

            }else if(msg.what == MSG_EXCEPTION){
                mWaitDlg.dismiss();
                Toast.makeText(getApplicationContext(),"请求异常，请长按摄像头复位按钮进行重置",Toast.LENGTH_LONG).show();
            }


        }
    };
    private ImageView mIvNavAvatar;
    private TextView mTvUserInfoName;
    private TextView mTvUserInfoIsCert;

    private CustomPopWindow mCustomPopWindow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushManager pushManager = PushManager.getInstance();
        pushManager.initialize(getApplicationContext(),com.wzlab.smartsecurity.service.PushService.class);
        phone = Config.getCachedPhone(getApplicationContext());
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        boolean i = pushManager.bindAlias(getApplicationContext(),phone,phone);
        Log.e(TAG, "onCreate: "+ i );
      //  EZOpenSDK.getInstance().setAccessToken("at.352z2nh08pvohywddanm9w8j2bm2qsl2-3d2b80xfa6-0s7s9eu-1e7eqzjvm");

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("首页");
        setSupportActionBar(toolbar);



        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initView();
        initNavView();





    }

    private void initNavView() {

      //  drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navView = navigationView.getHeaderView(0);
        mTvUserInfoName = navView.findViewById(R.id.tv_nav_user_name);
        mTvUserInfoIsCert = navView.findViewById(R.id.tv_nav_user_is_cert);
        mIvNavAvatar = navView.findViewById(R.id.iv_nav_avatar);
        mIvNavAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PersonalCenterActivity.class);
                intent.putExtra("avatarURL",userInfoAvatarURL);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
            }
        });


    }


    private void initView() {


        mVpMainContainer = findViewById(R.id.vp_main_container);
        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        Fragment deviceTabContainerFragment = new DeviceTabContainerFragment();

        Fragment alarmFragment = new AlarmFragment();
        Fragment advertisementFragment = new AdvertisementFragment();
        Fragment meFragment = new MeFragment();

        mFragmentList.add(deviceTabContainerFragment);
        mFragmentList.add(alarmFragment);
        mFragmentList.add(advertisementFragment);
        mFragmentList.add(meFragment);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mVpMainContainer.setAdapter(mViewPagerAdapter);






        //设置底部的导航菜单
        int[] iconNormal = {R.drawable.ic_bottom_nav_bar_home,R.drawable.ic_bottom_nav_bar_alarm,R.drawable.ic_bottom_nav_bar_msg,R.drawable.ic_bottom_nav_bar_me};
        int[] iconFocus = {R.drawable.ic_bottom_nav_bar_home_focus,R.drawable.ic_bottom_nav_bar_alarm_focus,R.drawable.ic_bottom_nav_bar_msg_focus,R.drawable.ic_bottom_nav_bar_me_focus};
        final String[] text = {"首页","报警","消息","我的"};
        mBottomNavMenuBar = findViewById(R.id.bottom_nav_menu_bar);
        mBottomNavMenuBar.setIconRes(iconNormal)
                .setIconResSelected(iconFocus)
                .setTextRes(text)
                .setSelected(0);
        mBottomNavMenuBar.setOnItemSelectedListener(new BottomNavMenuBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                mVpMainContainer.setCurrentItem(position);
                toolbar.setTitle(text[position]);
                if(position != 0){

                    mMenu.findItem(R.id.action_add_device).setVisible(false);
                }else{
                    mMenu.findItem(R.id.action_add_device).setVisible(true);
                }

            }
        });

        mBottomNavMenuBar.setOnItemReSelectedListener(new BottomNavMenuBar.OnItemReSelectedListener() {
            @Override
            public void onItemReSelected(int position) {

            }
        });

        mFlMainContainer = findViewById(R.id.fl_main_container);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        // 开启侧边栏滑动
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.\

        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_setting_app_permission) {
            AppConfigUtil.settingAppPermission(getApplicationContext());
            return true;
        }else if(id == R.id.action_add_device){
            // TODO
            showPopMenu();

//            Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
//            //跳转到扫描二维码页面
//            startActivityForResult(intent,1001);
            return true;
        } else if(id == R.id.action_setting_app_details){
            AppConfigUtil.AppDetailsSetting(getApplicationContext());
        }else if(id == R.id.action_setting_notification){
            AppConfigUtil.settingNotification(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && resultCode== Activity.RESULT_OK)
        {

            String deviceInfo=data.getStringExtra(CaptureActivity.KEY_DATA);
            Intent intent = new Intent(MainActivity.this,SelectLocationActivity.class);
            if(deviceInfo.length()>10 && ((deviceInfo.substring(8,9).equals("#") && (deviceTypeForBind.equals("1")))||(deviceInfo.substring(10,11).equals("#") && (deviceTypeForBind.equals("2"))))) {

                intent.putExtra("deviceInfo", deviceInfo);
                intent.putExtra("deviceType", deviceTypeForBind);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(),"无效二维码",Toast.LENGTH_LONG).show();
            }
          //  finish();

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
                    mWaitDlg.show();


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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
       // toolbar.setVisibility(View.GONE);
        if (id == R.id.nav_update_user_info) {
            // Handle the camera action
           // startActivity(new Intent(this,UpdateUserInfoActivity.class));
            Intent intent = new Intent(MainActivity.this,PersonalCenterActivity.class);
            intent.putExtra("avatarURL",userInfoAvatarURL);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);


        }  else if (id == R.id.nav_alarm_log) {
            mVpMainContainer.setCurrentItem(1);
            toolbar.setTitle("报警");
            mBottomNavMenuBar.setSelected(1);
        } else if (id == R.id.nav_repair) {
            Fragment fragment = new DeviceFaultReportFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_main_container, fragment).commitAllowingStateLoss();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            startActivity(new Intent(MainActivity.this, FeedBackActivity.class));

        } else if (id == R.id.nav_logout){
            String token = Config.getCachedToken(getApplicationContext());
            new Logout(token, new Logout.SuccessCallback() {
                @Override
                public void onSuccess(String msg) {
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    EZOpenSDK.getInstance().setAccessToken(null);
                }
            }, new Logout.FailCallback() {
                @Override
                public void onFail(String msg) {
                   // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                }
            });
            // 清除缓存
            Config.clearCache(getApplicationContext());
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
            finish();

        } else if (id == R.id.nav_shut_down) {
            finish();
        }

       // drawer = findViewById(R.id.drawer_layout);
         
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 头像加载放在这里，为了修改头像后能及时改变

        Log.e(TAG, "onResume: 进来了" );
        getUserBasicInfo();

    }



    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从推送消息点进来
        boolean isAlarming = intent.getBooleanExtra("isAlarming",false);
        boolean isForeground = intent.getBooleanExtra("isForeground",false);
        if(isAlarming && isForeground){

            mVpMainContainer.setCurrentItem(1);
            toolbar.setTitle("报警");
            mBottomNavMenuBar.setSelected(1);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogCustom);
            builder.setTitle("报警通知")
                    .setMessage("有新的报警信息，详情请查看报警记录")
                    .setNegativeButton("知道了",null)
                    .show();



        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PushManager pushManager = PushManager.getInstance();
        pushManager.stopService(getApplicationContext());
//        pushManager.initialize(this.getApplicationContext(), com.wzlab.smartsecurity.service.PushService.class);
//        // 注册消息接收服务
//        pushManager.registerPushIntentService(getApplicationContext(),IntentService.class);
    }

    // 获取用户基本信息
    public void getUserBasicInfo(){
        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_USER_BASIC_INFO, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            userInfoAvatarURL = jsonObject.getString("avatar");
                            userInfoName = DataParser.getData(jsonObject.getString("name"),"未实名");
                            userInfoIsCert = DataParser.getData(jsonObject.getString("is_cert"),"未认证").equals("1")?"已认证":"未认证";
                            EZOpenSDK.getInstance().setAccessToken(jsonObject.getString("access_token"));
                            // 下载头像
                            String avatarPath = getExternalFilesDir(null).getPath()+"/avatar/"+phone+".png";
                            File file = new File(avatarPath);
                            // 预设图片

                            if(file.exists()){//如果有缓存，直接显示
                                bmAvatar = BitmapFactory.decodeFile(avatarPath);
                                if(bmAvatar != null){
                                    Message message = new Message();
                                    message.what = LOAD_USER_INFO_ALL_SUCCESS;
                                    handler.sendMessage(message);
                                }else{
                                    Message message = new Message();
                                    message.what = LOAD_USER_INFO_TEXT_SUCCESS;
                                    handler.sendMessage(message);
                                }


                            }else if(userInfoAvatarURL!=null && userInfoAvatarURL.length()>10 ){
                                new DownloadImageTask().execute(userInfoAvatarURL,getExternalFilesDir(null).getPath()+"/avatar",phone);
//
                            }else{
                                Message message = new Message();
                                message.what = LOAD_USER_INFO_TEXT_SUCCESS;
                                handler.sendMessage(message);
                            }


                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"数据解析异常",Toast.LENGTH_SHORT).show();
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(),"未能链接服务器",Toast.LENGTH_SHORT).show();
            }
        },Config.KEY_PHONE, phone);
    }


    // 通过url下载头像并存储到本地
    class DownloadImageTask extends AsyncTask<String, Integer, Void> {

        protected Void doInBackground(String... params) {
            bmAvatar = GraphProcess.downLoadImage((String)params[0]);
            if(bmAvatar != null){
                GraphProcess.savaImage(bmAvatar,(String)params[1],(String)params[2]);
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(bmAvatar != null){
                Message message = new Message();
                message.what = LOAD_USER_INFO_ALL_SUCCESS;
                handler.sendMessage(message);
            }
        }

    }


    /**
     * 显示popup window,用于选择添加的设备类型
     */
    private void showPopMenu(){
        View contentView = LayoutInflater.from(this).inflate(R.layout.content_pop_menu,null);
        Button mBtnAddAlarmModel = contentView.findViewById(R.id.btn_add_alarm_model);
        Button mBtnAddOneButtonDevice = contentView.findViewById(R.id.btn_add_one_button_device);
        Button mBtnAddCamera = contentView.findViewById(R.id.btn_add_camera);

        mBtnAddAlarmModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "1";
                Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,1001);
                mCustomPopWindow.dissmiss();
            }
        });
        mBtnAddOneButtonDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "2";
                Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,1001);
                mCustomPopWindow.dissmiss();
            }
        });
        mBtnAddCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceTypeForBind = "3";
                Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
                //跳转到扫描二维码页面
                startActivityForResult(intent,Config.SCAN_QR_CODE_TO_ADD_CAMERA);
                mCustomPopWindow.dissmiss();
            }
        });

        mCustomPopWindow=new CustomPopWindow.PopupWindowBuilder(this)
                .setView(contentView)
                .enableBackgroundDark(true)
                .create()
                .showAtLocation(findViewById(R.id.fl_main_container), Gravity.BOTTOM,0,0);
    }


    /*
   点击两次返回才退出程序
 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {

        if (!isExit) {

                isExit = true;
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();


            // 利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed(ACTION_EXIT, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

}
