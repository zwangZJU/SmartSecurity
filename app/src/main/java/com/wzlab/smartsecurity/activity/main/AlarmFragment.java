package com.wzlab.smartsecurity.activity.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.AlarmLogAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.po.AlarmLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AlarmFragment extends Fragment {

    private ImageView mIvSos;

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<AlarmLog> alarmLogList = new ArrayList<>();
        AlarmLog alarmLog = new AlarmLog();
        alarmLog.setAlarm_time("dd");
        alarmLogList.add(alarmLog);
        alarmLogList.add(alarmLog);
        alarmLogList.add(alarmLog);
        alarmLogList.add(alarmLog);
        alarmLogList.add(alarmLog);
        alarmLogList.add(alarmLog);

        RecyclerView mRvAlarmLog = view.findViewById(R.id.rv_alarm_log);
        mRvAlarmLog.setLayoutManager(new LinearLayoutManager(getContext()));
        AlarmLogAdapter alarmLogAdapter = new AlarmLogAdapter(getContext(), alarmLogList);
        mRvAlarmLog.setAdapter(alarmLogAdapter);



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
                            break;
                        default:

                            break;
                    }
                    Toast.makeText(getContext(),jsonObject.getString(Config.RESULT_MESSAGE),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"数据异常",Toast.LENGTH_SHORT).show();
                }

            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                Toast.makeText(getContext(),"未能链接服务器",Toast.LENGTH_SHORT).show();
            }
        },Config.KEY_PHONE, phone);
    }


}
