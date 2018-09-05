package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.wzlab.smartsecurity.Listener.MyOrientationListener;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.AlarmLogAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.AlarmLog;
import com.wzlab.smartsecurity.widget.ClearableEditText;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class AlarmFragment extends Fragment {

    private ImageView mIvSos;
    private ArrayList<AlarmLog> alarmLogList;
    private LoadingLayout loadingLayout;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc=true;
    private MyLocationConfiguration.LocationMode locationMode;

    public LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();
    private MyOrientationListener myOrientationListener;
    private float mCurrentDirection;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private float mRadius;


    private static final int KEY_FINISH_REFRESH = 3;
    private static final int MSG_SHOW_REAL_TIME_LOCATION = 400;
    private static final int MSG_UPLOAD_REAL_TIME_LOCATION_FAIL = 401;
    private static final int MSG_CANCEL_UPLOAD = 402;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == Config.KEY_LOADING_EMPTY){
                loadingLayout.showEmpty();
            }else if(msg.what == Config.KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         getAlarmLogList();
                    }
                });

            }else if(msg.what == Config.KEY_LOADING_SUCCESS){

                loadingLayout.showContent();
            }else if(msg.what == KEY_FINISH_REFRESH){
                swipeRefreshLayout.setRefreshing(false);

            }else if(msg.what == MSG_SHOW_REAL_TIME_LOCATION){
                showRealTimePositioning();
            }else if(msg.what == MSG_UPLOAD_REAL_TIME_LOCATION_FAIL){
                if(msg.arg1 == 1){
                    Toast.makeText(getContext(),"未能连接服务器,请检查网络",Toast.LENGTH_SHORT).show();
                    mapView.onPause();
                    mapView.onDestroy();
                    mBaiduMap.setMyLocationEnabled(false);
                    mLocationClient.stop();
                    myOrientationListener.stop();
                    alertDialog.dismiss();

                }else if(msg.arg1 == 2){
                    Toast.makeText(getContext(),"服务器异常",Toast.LENGTH_SHORT).show();
                    mapView.onPause();
                    mapView.onDestroy();
                    mBaiduMap.setMyLocationEnabled(false);
                    mLocationClient.stop();
                    myOrientationListener.stop();
                    alertDialog.dismiss();
                }

            }else if(MSG_CANCEL_UPLOAD == msg.what){
                Toast.makeText(getContext(),"报警中心已经接警，请原地等待救援",Toast.LENGTH_LONG).show();
                mapView.onPause();
                mapView.onDestroy();
                mBaiduMap.setMyLocationEnabled(false);
                mLocationClient.stop();
                myOrientationListener.stop();
                alertDialog.dismiss();
            }
        }
    };



    private RecyclerView mRvAlarmLog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MapView mapView;
    private String phone;
    private AlertDialog alertDialog;
    private String addr;
    private String city;
    private String district;
    private String street;
    private String describe;


    public AlarmFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getContext().getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvAlarmLog = view.findViewById(R.id.rv_alarm_log);
        loadingLayout = view.findViewById(R.id.loading_layout_alarm_log);
        loadingLayout.setEmptyFloatButtonVisible(false);
        alarmLogList = new ArrayList<>();
        getAlarmLogList();

        swipeRefreshLayout = view.findViewById(R.id.srl_refresh_alarm_log);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            getAlarmLogList();
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
//        AlarmLog alarmLog = new AlarmLog();
//        alarmLog.setAlarm_time("9月16日");
//        alarmLog.setDevice_id("www");
//        alarmLog.setUser_address("dddddddd");
//        alarmLog.setType_("手动报警");
//        alarmLog.setReason_("未知");
//        alarmLogList.add(alarmLog);
//        alarmLogList.add(alarmLog);
//        alarmLogList.add(alarmLog);
//        alarmLogList.add(alarmLog);
//        alarmLogList.add(alarmLog);
//        alarmLogList.add(alarmLog);







        mIvSos = view.findViewById(R.id.iv_sos);
        mIvSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarm();
               // Toast.makeText(getContext(),"sos",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 联网报警的
    private void alarm() {
        phone = Config.getCachedPhone(getContext());
        // 报警的联网操作
        new NetConnection(Config.SERVER_URL + Config.ACTION_SOS, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);

                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            Message message = new Message();
                            message.what = MSG_SHOW_REAL_TIME_LOCATION;
                            handler.sendMessage(message);

                            break;
                        default:

                            break;
                    }
                    Toast.makeText(getContext(),jsonObject.getString(Config.RESULT_MESSAGE),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"数据异常",Toast.LENGTH_SHORT).show();
                    Message message = new Message();
                    message.what = Config.KEY_LOADING_ERROR;
                    handler.sendMessage(message);
                }

            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getContext(),"未能链接服务器",Toast.LENGTH_SHORT).show();
                Message message = new Message();
                message.what = Config.KEY_LOADING_ERROR;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone);
    }

    // 在AlertDialog中展示实时定位信息，并每隔两秒上传一次最新的定位数据，直到报警结束位置
    private void showRealTimePositioning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setTitle("报警通知")
                .setMessage("正在向报警中心发送您的定位信息")
                .setPositiveButton("取消报警",null)
                .setNegativeButton("停止发送",null)
                .setNeutralButton("继续报警",null)
                .setView(R.layout.layout_real_time_positioning_for_sos)
                .setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        mapView = alertDialog.findViewById(R.id.bmapView_sos);
        final LinearLayout ll = alertDialog.findViewById(R.id.al_ll);
        final ClearableEditText mEtPhone = alertDialog.findViewById(R.id.et_relese_alarm_phone);
        final ClearableEditText mEtReason = alertDialog.findViewById(R.id.et_relese_alarm_reason);
        showLocation();
        mapView.onResume();
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(alertDialog.isShowing()) {
                    uploadRealTimeLocation(String.valueOf(mCurrentLatitude), String.valueOf(mCurrentLongitude));
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mapView.getVisibility() == View.VISIBLE &&ll.getVisibility() == View.GONE){
                    mapView.setVisibility(View.GONE);
                    ll.setVisibility(View.VISIBLE);
                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorPrimary));
                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("提交以取消报警");
                }else if(ll.getVisibility() == View.VISIBLE){
                     String verifyPhone = mEtPhone.getText().toString();
                     String reason = mEtReason.getText().toString();
                    if(TextUtils.isEmpty(verifyPhone) || TextUtils.isEmpty(reason)){
                        Toast.makeText(getContext(),"请将信息填写完整",Toast.LENGTH_SHORT).show();
                    }else if(!verifyPhone.equals(phone)){
                        Toast.makeText(getContext(),"手机号与登录信息不符，请重新输入",Toast.LENGTH_SHORT).show();
                    }else {

                        cancelAlarm(reason);
                      //  Toast.makeText(getContext(),"报警已解除",Toast.LENGTH_SHORT).show();

                    }
                }else if(mapView.getVisibility() == View.GONE && ll.getVisibility() == View.GONE){
                    alertDialog.setMessage("正在持续向报警中心发送您的定位信息");
                    mapView.setVisibility(View.VISIBLE);
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("取消报警");
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white_top));
                }



            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.white_top));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.white_top));
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("取消报警");

            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.onPause();
                mapView.onDestroy();
                mBaiduMap.setMyLocationEnabled(false);
                mLocationClient.stop();
                myOrientationListener.stop();
                alertDialog.dismiss();
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.white_top));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white_top));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
        getAlarmLogList();



    }

    // 向服务器发送取消报警的请求
    private void cancelAlarm(String reason) {
        new NetConnection(Config.SERVER_URL + Config.ACTION_CANCEL_ALARM, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getContext(),"报警已取消",Toast.LENGTH_SHORT).show();
                mapView.onPause();
                mapView.onDestroy();
                mBaiduMap.setMyLocationEnabled(false);
                mLocationClient.stop();

                myOrientationListener.stop();
                alertDialog.dismiss();
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getContext(),"未能连接服务器",Toast.LENGTH_SHORT).show();
            }
        },Config.KEY_PHONE,phone,"reason",reason);

    }

    private void showLocation() {
        isFirstLoc=true;
        mBaiduMap = mapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        // 用百度地图的函数获取定位信息
        mLocationClient = new LocationClient(getContext().getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        //注册监听函数
        //设置定位方式
        initLocation();
        // 初始化传感器
        initOritationListener();

        mBaiduMap.setMyLocationEnabled(true); //开启定位图层
        MyLocationConfiguration.LocationMode mode =  MyLocationConfiguration.LocationMode.NORMAL;// 罗盘模式
        boolean enableDirection = true;  // 设置允许显示方向
        BitmapDescriptor myLocation = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_sos);
        //设置缩放
        MapStatusUpdate scale = MapStatusUpdateFactory.zoomTo(19);
        mBaiduMap.setMapStatus(scale);
        //设置定位方式
        MyLocationConfiguration config = new MyLocationConfiguration(mode, enableDirection, myLocation);
        mBaiduMap.setMyLocationConfiguration(config);
        mLocationClient.start();
        myOrientationListener.start();
    }

    public void getAlarmLogList(){
        String phone = Config.getCachedPhone(getContext());
        alarmLogList.clear();
        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_ALARM_LOG_LIST, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:

                                //TODO

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                Gson gson = new Gson();
                                for(int i=0;i<jsonArray.length();i++){
                                    alarmLogList.add(gson.fromJson(jsonArray.get(i).toString(),AlarmLog.class));
                                }

                            if(alarmLogList.size()>0){
                                    //arrayList逆序
                                //Collections.reverse(alarmLogList);
                                mRvAlarmLog.setLayoutManager(new LinearLayoutManager(getContext()));
                                AlarmLogAdapter alarmLogAdapter = new AlarmLogAdapter(getContext(), alarmLogList);
                                mRvAlarmLog.setAdapter(alarmLogAdapter);
                                Message message = new Message();
                                message.what = Config.KEY_LOADING_SUCCESS;
                                handler.sendMessage(message);
                            }else{
                                Message message = new Message();
                                message.what = Config.KEY_LOADING_EMPTY;
                                handler.sendMessage(message);
                            }


                            break;
                        default:
                            Message message = new Message();
                            message.what = Config.KEY_LOADING_EMPTY;
                            handler.sendMessage(message);
                           // loadingLayout.setErrorText("获取数据失败");

                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = Config.KEY_LOADING_ERROR;
                    handler.sendMessage(message);
                    loadingLayout.setErrorText("数据解析异常");
                   // Toast.makeText(getContext(),"数据解析异常",Toast.LENGTH_SHORT).show();
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what = Config.KEY_LOADING_ERROR;
                handler.sendMessage(message);
                loadingLayout.setErrorText("未能链接到服务器");

            }
        },Config.KEY_PHONE, phone);
    }

    /**
     * 初始化方向传感器
     */
    private void initOritationListener()
    {
        myOrientationListener = new MyOrientationListener(
                getContext().getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener()
                {
                    @Override
                    public void onOrientationChanged(float x)
                    {
                        mCurrentDirection =  x;
                        MyLocationData.Builder builder = new MyLocationData.Builder();
                        builder.accuracy(mRadius);
                        // builder.direction(location.getDirection());
                        builder.direction(mCurrentDirection);
                        builder.latitude(mCurrentLatitude);
                        builder.longitude(mCurrentLongitude);
                        // Log.d(TAG, "onReceiveLocation: "+mXDirection);


                       // uploadRealTimeLocation(String.valueOf(mCurrentLatitude),String.valueOf(mCurrentLongitude));
                        MyLocationData locationData = builder.build();
                        mBaiduMap.setMyLocationData(locationData);
                        //   Log.d(TAG, "onOrientationChanged: " + x);
                    }
                });
    }

    private void uploadRealTimeLocation(String latitude, String longitude) {
        new NetConnection(Config.SERVER_URL + Config.ACTION_UPLOAD_REAL_TIME_LOCATION, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.getString("status").equals("1")){

                    }else if(jsonObject.getString("status").equals("2")){

                        alertDialog.setMessage("报警中心已接到报警，是否还要持续发送定位信息？");
                        mapView.setVisibility(View.GONE);
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("持续发送");
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

                        Message message = new Message();
                        message.what = MSG_CANCEL_UPLOAD;

                        handler.sendMessage(message);

                    } else{
                        Message message = new Message();
                        message.what = MSG_UPLOAD_REAL_TIME_LOCATION_FAIL;
                        message.arg1 = 2;
                        handler.sendMessage(message);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = MSG_UPLOAD_REAL_TIME_LOCATION_FAIL;
                    message.arg1 = 2;
                    handler.sendMessage(message);
                }


            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what = MSG_UPLOAD_REAL_TIME_LOCATION_FAIL;
                message.arg1 = 1;
                handler.sendMessage(message);

            }
        },Config.KEY_PHONE, phone, "latitude", latitude, "longitude", longitude, "address", city+district+street+describe);
    }


    //设置定位SDK的定位方式
    private void initLocation(){

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        //option.setIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        //option.setWifiValidTime(5*60*1000);
        //可选，7.2版本新增能力，如果您设置了这个接口，首次启动定位时，会先判断当前WiFi是否超出有效期，超出有效期的话，会先重新扫描WiFi，然后再定位
        mLocationClient.setLocOption(option);
    }



    public class MyLocationListener extends BDAbstractLocationListener {


        public void onReceiveLocation(BDLocation location) {

            //获取定位结果
            location.getTime();    //获取定位时间
            location.getLocationID();    //获取定位唯一ID，v7.2版本新增，用于排查定位问题
            location.getLocType();    //获取定位类型
            //  Log.d(TAG, "onReceiveLocation: "+ location.getLocType());
            mCurrentLatitude = location.getLatitude();    //获取纬度信息
            mCurrentLongitude = location.getLongitude();    //获取经度信息
            mRadius = location.getRadius();    //获取定位精准度
            //获取地址信息
            location.getAddrStr();
            location.getCountry();    //获取国家信息
            location.getCountryCode();    //获取国家码
            //获取城市信息
            city = location.getCity();
            location.getCityCode();    //获取城市码
            //获取区县信息
            district = location.getDistrict();
            //获取街道信息
            street = location.getStreet();
            location.getStreetNumber();    //获取街道码
            //获取当前位置描述信息
            describe = location.getLocationDescribe();
            location.getPoiList();    //获取当前位置周边POI信息

            location.getBuildingID();    //室内精准定位下，获取楼宇ID
            location.getBuildingName();    //室内精准定位下，获取楼宇名称
            location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息

            //设置地图中心
            if(isFirstLoc){
                MapStatusUpdate setCenter = MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));

                mBaiduMap.animateMapStatus(setCenter);

                isFirstLoc = false;
            }

            // TODO
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.accuracy(mRadius);
            // builder.direction(location.getDirection());
            builder.direction(mCurrentDirection);
            builder.latitude(mCurrentLatitude);
            builder.longitude(mCurrentLongitude);
            Log.e("AlarmFragment", "onReceiveLocation: "+mCurrentLatitude);

            MyLocationData locationData = builder.build();
            mBaiduMap.setMyLocationData(locationData);

            if (location.getLocType() == BDLocation.TypeGpsLocation){

                //当前为GPS定位结果，可获取以下信息
                location.getSpeed();    //获取当前速度，单位：公里每小时
                location.getSatelliteNumber();    //获取当前卫星数
                location.getAltitude();    //获取海拔高度信息，单位米
                location.getDirection();    //获取方向信息，单位度


            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){

                //当前为网络定位结果，可获取以下信息
                location.getOperators();    //获取运营商信息

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                //当前为网络定位结果

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                //当前网络定位失败
                //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                //当前网络不通

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
                //可进一步参考onLocDiagnosticMessage中的错误返回码

            }
        }
    }

}
