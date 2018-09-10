package com.wzlab.smartsecurity.activity.main;



import android.annotation.SuppressLint;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.donkingliang.labels.LabelsView;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.AccountActivity;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.PoiListAdapter;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class SelectLocationActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private static final String TAG = "SelectLocationActivity";
    private String provinceCityArea;
    private String addrInfo;
    private String latLngInfo;

    private boolean isFirstLoc=true;
    private MyLocationConfiguration.LocationMode locationMode;

    public LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();

    private float mCurrentDirection;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private float mRadius;
    private GeoCoder mSearch;
    private RecyclerView mRvPoiList;
    private ArrayList<PoiInfo> mPoiList;
    private String deviceInfo;
    private String locLabel = null;
    private String deviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_select_location);

        // TitleBar 的 返回按钮监听事件
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        deviceInfo = intent.getStringExtra("deviceInfo");
        deviceType = intent.getStringExtra("deviceType");

        // 地图显示
        mMapView = findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        // 设置定位
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myLocationListener);
        initLocation();
        mBaiduMap.setMyLocationEnabled(true); //开启定位图层
       // MyLocationConfiguration.LocationMode mode =  MyLocationConfiguration.LocationMode.NORMAL;// 罗盘模式
      //  boolean enableDirection = true;  // 设置允许显示方向
       // BitmapDescriptor myLocation = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);
        //设置缩放
        MapStatusUpdate scale = MapStatusUpdateFactory.zoomTo(20);
        mBaiduMap.setMapStatus(scale);
        //设置定位方式
     //   MyLocationConfiguration config = new MyLocationConfiguration(mode, enableDirection, null);
      //  mBaiduMap.setMyLocationConfiguration(config);
        mLocationClient.start();

        mSearch = GeoCoder.newInstance();

        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getApplicationContext(),"请连接网络",Toast.LENGTH_SHORT).show();
                }else{

                    PoiInfo accurateLoc = new PoiInfo();
                    accurateLoc.address = reverseGeoCodeResult.getAddress();
                    accurateLoc.name = reverseGeoCodeResult.getSematicDescription();
                    provinceCityArea = accurateLoc.address.split("区")[0]+"区";
                    accurateLoc.location = reverseGeoCodeResult.getLocation();
                    final String latlng = accurateLoc.location.toString();





                    mPoiList = (ArrayList<PoiInfo>) reverseGeoCodeResult.getPoiList();

                    mPoiList.add(0,accurateLoc);
                    // 列出POI
                    mRvPoiList = findViewById(R.id.rv_poi_list);
                    mRvPoiList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    PoiListAdapter poiListAdapter = new PoiListAdapter(getApplicationContext(), mPoiList );
                    mRvPoiList.setAdapter(poiListAdapter);
                    poiListAdapter.setOnItemClickListener(new PoiListAdapter.OnItemClickListener() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onItemClick(View view, int position) {
                            PoiInfo poiInfo = mPoiList.get(position);
                            if(position == 0){
                                addrInfo = poiInfo.address+poiInfo.name;
                             //   Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
                            }else{
                                // 去除地址中重复的部分，到“区”为止
                                String addr = poiInfo.address;
                                String[] s = addr.split("区");
                                if(s.length>1){
                                    int start = s[0].length();
                                    addr = addr.substring(start+1);
                                    addrInfo = provinceCityArea + addr +poiInfo.name;
                                 //   Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
                                }else{
                                    addrInfo = provinceCityArea + addr +poiInfo.name;
                                 //   Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
                                }

                            }

                            latLngInfo = poiInfo.location.toString();
                            AlertDialog.Builder builder= new AlertDialog.Builder(SelectLocationActivity.this);
                            builder.setTitle("确定选择如下地址？")
                                    .setMessage(addrInfo)
                                    .setIcon(getResources().getDrawable(R.drawable.ic_my_location))
                                    .setPositiveButton("确定", null)
                                    .setNegativeButton("关闭", null)
                                    .setView(R.layout.content_alert_dialog_edit_text)
                                    .setCancelable(false);
                            final AlertDialog alertDialog = builder.create();

                            alertDialog.show();
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                            // TODO 获取不到组件
                            // 设置标签
                            LabelsView labelsView = alertDialog.findViewById(R.id.labels);
                            final ArrayList<String> labels = new ArrayList<>();
                            labels.add("住宅");
                            labels.add("公司");
                            labels.add("学校");
                            labels.add("小区");
                            labels.add("其他");
                            labelsView.setLabels(labels);

                            labelsView.setOnLabelSelectChangeListener(new LabelsView.OnLabelSelectChangeListener() {
                                @Override
                                public void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position) {

                                    locLabel = (String)data;
                                    Log.e(TAG, "onLabelSelectChange: "+ label );
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }
                            });


                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @SuppressLint("ClickableViewAccessibility")
                                @Override
                                public void onClick(View view) {



                                    EditText editText = alertDialog.findViewById(R.id.et_house_num_dialog);

                                    editText.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                        }

                                        @Override
                                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                        }

                                        @Override
                                        public void afterTextChanged(Editable editable) {
                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                                        }
                                    });




                                    String houseNum = editText.getText().toString();
                                    if(TextUtils.isEmpty(houseNum)){
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        Toast.makeText(SelectLocationActivity.this,"请填写门牌号信息",Toast.LENGTH_SHORT).show();
                                    }else if(locLabel == null){
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        Toast.makeText(SelectLocationActivity.this,"请选择标签",Toast.LENGTH_SHORT).show();

                                    }else{
                                        alertDialog.dismiss();
                                        String info = addrInfo + houseNum + "#" + latLngInfo;
                                        String phone = Config.getCachedPhone(getApplicationContext());
                                        if(TextUtils.isEmpty(phone)){
                                            startActivity(new Intent(SelectLocationActivity.this, AccountActivity.class));
                                        }else{

                                            GetDeviceInfo.deviceBinding(phone, locLabel, Config.TYPE_ROLE, deviceInfo, info, deviceType, new GetDeviceInfo.SuccessCallback() {
                                                @Override
                                                public void onSuccess(ArrayList list, ArrayList camera, String msg) {
                                                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SelectLocationActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                            }, new GetDeviceInfo.FailCallback() {
                                                @Override
                                                public void onFail(String msg) {
                                                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SelectLocationActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                            });
                                        }

                                      //  Toast.makeText(SelectLocationActivity.this,info,Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });



                        }
                    });


                    Log.d(TAG, "onGetReverseGeoCodeResult: ");
                }



            }
        });

        //获取反向地理编码结果
        // TODO
       // LatLng ptCenter = getMapCenterPoint();








    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mSearch.destroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();

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

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                LatLng ptCenter = mapStatus.target;
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));

            }
        });
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
            location.getAddrStr();    //获取地址信息
            location.getCountry();    //获取国家信息
            location.getCountryCode();    //获取国家码
            location.getCity();    //获取城市信息
            location.getCityCode();    //获取城市码
            location.getDistrict();    //获取区县信息
            location.getStreet();    //获取街道信息
            location.getStreetNumber();    //获取街道码
            location.getLocationDescribe();    //获取当前位置描述信息
            location.getPoiList();    //获取当前位置周边POI信息

            location.getBuildingID();    //室内精准定位下，获取楼宇ID
            location.getBuildingName();    //室内精准定位下，获取楼宇名称
            location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息

            //设置地图中心
            if(isFirstLoc){
                MapStatusUpdate setCenter = MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));

                mBaiduMap.animateMapStatus(setCenter);
                Log.d(TAG, "onReceiveLocation: " + isFirstLoc);
                isFirstLoc = false;
            }

            // TODO
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.accuracy(mRadius);
            // builder.direction(location.getDirection());
          //  builder.direction(mCurrentDirection);
            builder.latitude(mCurrentLatitude);
            builder.longitude(mCurrentLongitude);
            // Log.d(TAG, "onReceiveLocation: "+mXDirection);

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
