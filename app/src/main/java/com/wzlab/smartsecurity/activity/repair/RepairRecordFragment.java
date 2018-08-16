package com.wzlab.smartsecurity.activity.repair;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.RepairLogAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.RepairLog;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RepairRecordFragment extends Fragment {

    private RecyclerView recyclerView;
    private LoadingLayout loadingLayout;
    private ArrayList<RepairLog> repairLogList;
    private String phone;

    private static final int KEY_LOADING_ERROR = -1;
    private static final int KEY_LOADING_EMPTY = 0;
    private static final int KEY_LOADING_SUCCESS = 1;
    private static final int KEY_LOADING_LOADING = 2;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == KEY_LOADING_EMPTY){
                loadingLayout.setEmptyFloatButtonVisible(false);
                loadingLayout.showEmpty();



            }else if(msg.what == KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getRepairLogList();
                    }
                });

            }else if(msg.what == KEY_LOADING_SUCCESS){

                loadingLayout.showContent();
            }
        }
    };
    public RepairRecordFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repair_log, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phone = Config.getCachedPhone(getContext());
        loadingLayout = view.findViewById(R.id.loading_layout_repair_log);

        recyclerView = view.findViewById(R.id.rv_repair_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getRepairLogList();


    }

    public void getRepairLogList(){
        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_REPAIR_LOG_LIST, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:

                            //TODO
                            repairLogList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for(int i=0;i<jsonArray.length();i++){
                                repairLogList.add(gson.fromJson(jsonArray.get(i).toString(),RepairLog.class));
                            }
                            RepairLogAdapter adapter = new RepairLogAdapter(getContext(),repairLogList);
                            recyclerView.setAdapter(adapter);
                            Message message = new Message();
                            message.what = KEY_LOADING_SUCCESS;
                            handler.sendMessage(message);

                            break;
                        default:
                             Message message1 = new Message();
                             message1.what = KEY_LOADING_EMPTY;
                             handler.sendMessage(message1);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingLayout.setErrorText("数据解析异常");
                    Message message = new Message();
                    message.what = KEY_LOADING_ERROR;
                    handler.sendMessage(message);

                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                loadingLayout.setErrorText("未能连接到服务器");
                Message message = new Message();
                message.what = KEY_LOADING_ERROR;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone);
    }
}
