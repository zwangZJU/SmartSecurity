package com.wzlab.smartsecurity.activity.main;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.gson.Gson;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.AlarmLogAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.AlarmLog;
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

    private static final int KEY_LOADING_ERROR = -1;
    private static final int KEY_LOADING_EMPTY = 0;
    private static final int KEY_LOADING_SUCCESS = 1;
    private static final int KEY_LOADING_LOADING = 2;
    private static final int KEY_FINISH_REFRESH = 3;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == KEY_LOADING_EMPTY){
                loadingLayout.showEmpty();

            }else if(msg.what == KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         getAlarmLogList();
                    }
                });

            }else if(msg.what == KEY_LOADING_SUCCESS){

                loadingLayout.showContent();
            }else if(msg.what == KEY_FINISH_REFRESH){
                swipeRefreshLayout.setRefreshing(false);

            }
        }
    };
    private RecyclerView mRvAlarmLog;
    private SwipeRefreshLayout swipeRefreshLayout;

    public AlarmFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        String phone = Config.getCachedPhone(getContext());
        // 报警的联网操作
        new NetConnection(Config.SERVER_URL + Config.ACTION_SOS, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);

                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
                            builder.setTitle("报警通知")
                                    .setMessage("你现在慌的一批")
                                    .setNegativeButton("知道了",null)
                                    .show();
                            getAlarmLogList();

                            break;
                        default:

                            break;
                    }
                    Toast.makeText(getContext(),jsonObject.getString(Config.RESULT_MESSAGE),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"数据异常",Toast.LENGTH_SHORT).show();
                    Message message = new Message();
                    message.what = KEY_LOADING_ERROR;
                    handler.sendMessage(message);
                }

            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getContext(),"未能链接服务器",Toast.LENGTH_SHORT).show();
                Message message = new Message();
                message.what = KEY_LOADING_ERROR;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone);
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
                                Collections.reverse(alarmLogList);
                                mRvAlarmLog.setLayoutManager(new LinearLayoutManager(getContext()));
                                AlarmLogAdapter alarmLogAdapter = new AlarmLogAdapter(getContext(), alarmLogList);
                                mRvAlarmLog.setAdapter(alarmLogAdapter);
                                Message message = new Message();
                                message.what = KEY_LOADING_SUCCESS;
                                handler.sendMessage(message);
                            }else{
                                Message message = new Message();
                                message.what = KEY_LOADING_EMPTY;
                                handler.sendMessage(message);
                            }


                            break;
                        default:
                            Message message = new Message();
                            message.what = KEY_LOADING_EMPTY;
                            handler.sendMessage(message);
                           // loadingLayout.setErrorText("获取数据失败");

                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = KEY_LOADING_ERROR;
                    handler.sendMessage(message);
                    loadingLayout.setErrorText("数据解析异常");
                   // Toast.makeText(getContext(),"数据解析异常",Toast.LENGTH_SHORT).show();
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what = KEY_LOADING_ERROR;
                handler.sendMessage(message);
                loadingLayout.setErrorText("未能链接到服务器");

            }
        },Config.KEY_PHONE, phone);
    }

}
