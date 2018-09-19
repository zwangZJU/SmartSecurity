package com.wzlab.smartsecurity.activity.message;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.MessageListAdapter;
import com.wzlab.smartsecurity.adapter.RecyclerViewNoBugLinearLayoutManager;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.Advert;
import com.wzlab.smartsecurity.widget.LoadingLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdvertisementFragment extends Fragment {


    private LoadingLayout loadingLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == Config.KEY_LOADING_EMPTY){
                loadingLayout.setEmptyFloatButtonVisible(false);
                loadingLayout.setEmptyImage(R.drawable.ic_without_msg);
                loadingLayout.setEmptyText("暂无消息");
                loadingLayout.showEmpty();
            }else if(msg.what == Config.KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setErrorText("未能连接服务器");
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getAdvertList();
                    }
                });

            }else if(msg.what == Config.KEY_LOADING_SUCCESS){
                Collections.reverse(advertList);
                messageListAdapter.setAdvertList(advertList);
                mRvMessageList.smoothScrollToPosition(0);

//                if( mRvMessageList.canScrollVertically(1)){
//                    mRvMessageList.smoothScrollToPosition(messageListAdapter.getItemCount()-1);
//                }

                loadingLayout.showContent();
              //

            }else if(msg.what == Config.KEY_FINISH_REFRESH){
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };
    private RecyclerView mRvMessageList;
    private ArrayList<Advert> advertList;
    private MessageListAdapter messageListAdapter;
    private String phone;


    public AdvertisementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        phone = Config.getCachedPhone(getContext());
        return inflater.inflate(R.layout.fragment_advertisement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingLayout = view.findViewById(R.id.loading_layout_msg_list);


        mRvMessageList = view.findViewById(R.id.rv_msg_list);
        mRvMessageList.setLayoutManager(new RecyclerViewNoBugLinearLayoutManager(getContext()));
        advertList = new ArrayList<>();
        messageListAdapter = new MessageListAdapter(getContext(),advertList);
        messageListAdapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(),MessageWebViewActivity.class);
                intent.putExtra("url",Config.ADVERT_HTML_URL+advertList.get(position).getUrl());
                startActivity(intent);
            }
        });
        mRvMessageList.setAdapter(messageListAdapter);


        swipeRefreshLayout = view.findViewById(R.id.srl_msg_list);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(){
                    @Override
                    public void run() {
                        getAdvertList();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message();
                        message.what = Config.KEY_FINISH_REFRESH;
                        handler.sendMessage(message);
                    }
                }.start();
            }
        });

        getAdvertList();

    }

    private void getAdvertList() {

        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_ADVERT_LIST, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getString(Config.KEY_STATUS).equals("1")){
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        Gson gson = new Gson();
                        advertList.clear();
                        for(int i=0;i<jsonArray.length();i++){
                            advertList.add(gson.fromJson(jsonArray.get(i).toString(),Advert.class));
                        }

                        Message message = new Message();
                        message.what = Config.KEY_LOADING_SUCCESS;
                        handler.sendMessage(message);


                    }else{
                        Message message = new Message();
                        message.what = Config.KEY_LOADING_EMPTY;
                        handler.sendMessage(message);
                    }


                    // Toast.makeText(getContext(),jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //
                    Message message = new Message();
                    message.what =  Config.KEY_LOADING_ERROR;
                    handler.sendMessage(message);
                }


            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Message message = new Message();
                message.what =  Config.KEY_LOADING_ERROR;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone);
    }
}
