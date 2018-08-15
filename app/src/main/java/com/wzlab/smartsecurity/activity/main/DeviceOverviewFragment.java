package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skateboard.zxinglib.CaptureActivity;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.start.StartActivity;
import com.wzlab.smartsecurity.adapter.DeviceOverviewAdapter;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Device;
import com.wzlab.smartsecurity.utils.CheckNetworkStatus;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import java.util.ArrayList;


public class DeviceOverviewFragment extends Fragment {

    private static final String TAG = "DeviceOverviewFragment";
    private static final int KEY_LOADING_ERROR = -1;
    private static final int KEY_LOADING_EMPTY = 0;
    private static final int KEY_LOADING_SUCCESS = 1;
    private static final int KEY_LOADING_LOADING = 2;
    private static final int KEY_FINISH_REFRESH = 3;
    private RecyclerView mRvDeviceOverview;
    private ArrayList<Device> deviceList;
    private DeviceOverviewAdapter deviceOverviewAdapter;
    private LoadingLayout loadingLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    public DeviceOverviewFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }





    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == KEY_LOADING_EMPTY){
                loadingLayout.showEmpty();
                //空白页的添加按钮
                loadingLayout.setOnEmptyButtonClickListener(new LoadingLayout.OnEmptyButtonClickListener() {
                    @Override
                    public void onClick() {
                      //  Toast.makeText(getContext(),"ddd",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getContext(), CaptureActivity.class);
                        //跳转到扫描二维码页面
                        startActivityForResult(intent,1001);

                    }
                });
            }else if(msg.what == KEY_LOADING_ERROR){
                loadingLayout.showError();
            }else if(msg.what == KEY_LOADING_SUCCESS){
                deviceOverviewAdapter = new DeviceOverviewAdapter(getContext(),deviceList);
                deviceOverviewAdapter.setOnItemClickListener(new DeviceOverviewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
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
                            startActivityForResult(intent,1001);
                        }


                        // getFragmentManager().beginTransaction().addToBackStack(null).add(deviceDetailFragment,)
                    }
                });
                mRvDeviceOverview.setAdapter(deviceOverviewAdapter);


                loadingLayout.showContent();
            }else if(msg.what == KEY_LOADING_LOADING){
                loadingLayout.showLoading();
            }else if(msg.what == KEY_FINISH_REFRESH){

                swipeRefreshLayout.setRefreshing(false);

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
//        swipeRefreshLayout.setOnRefreshListener(new CircleRefreshLayout.OnCircleRefreshListener() {
//            @Override
//            public void completeRefresh() {
//
//
//            }
//
//            @Override
//            public void refreshing() {
//                new Thread(){
//                    @Override
//                    public void run() {
//
//                        try {
//                            Thread.sleep(1500);
//
//                            Message msg = new Message();
//                            msg.what = FINISH_REFRESH;
//                            handler.sendMessage(msg);//                            handler.sendMessage(msg);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });




    }

    // 扫完二维码返回到这里
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && resultCode== Activity.RESULT_OK)
        {
            String phone = Config.getCachedPhone(getContext());
            String deviceInfo=data.getStringExtra(CaptureActivity.KEY_DATA);
            Intent intent = new Intent(getContext(),SelectLocationActivity.class);
            intent.putExtra("deviceInfo", deviceInfo);
            startActivity(intent);
            getActivity().finish();

        }
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
                    handlerMsg.what = KEY_LOADING_SUCCESS;
                }else{//如果为空
                    handlerMsg.what = KEY_LOADING_EMPTY;
                }
                handler.sendMessage(handlerMsg);
            }
        }, new GetDeviceInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message handlerMsg = new Message();
                handlerMsg.what = KEY_LOADING_ERROR;
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
}
