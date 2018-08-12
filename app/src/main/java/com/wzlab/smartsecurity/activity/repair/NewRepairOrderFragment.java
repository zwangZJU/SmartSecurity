package com.wzlab.smartsecurity.activity.repair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.skateboard.zxinglib.CaptureActivity;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class NewRepairOrderFragment extends Fragment {




    private EditText mEtRepairContent;
    private EditText mEtDeviceId;
    private ImageView mIvQRScanner;
    private CircularProgressButton mBtnSubmit;
    private Spinner spinner;
    private String spSelected;
    private String faultDescription;
    private String deviceId;


    public NewRepairOrderFragment() {
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
        return inflater.inflate(R.layout.fragment_new_repair_order, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = view.findViewById(R.id.spinner);
        final String[] spContent = new String[]{"请选择故障类型","硬件故障", "软件有bug", "就想吐槽"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spContent);  //创建一个数组适配器
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                spSelected = spinner.getItemAtPosition(position).toString();
                if(position == 0){
                    spSelected = spinner.getItemAtPosition(3).toString();
                }
              //  Toast.makeText(getContext(),spSelected,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spSelected = spContent[1];
            }
        });

        mBtnSubmit = view.findViewById(R.id.btn_submit);
        mBtnSubmit.setText("提交");
        mBtnSubmit.setIdleText("提交");
        mBtnSubmit.setCompleteText("提交成功");
        mBtnSubmit.setErrorText("提交失败");
        mIvQRScanner = view.findViewById(R.id.iv_repair_scanner);
        mIvQRScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),CaptureActivity.class);
                startActivityForResult(intent,1001);
            }
        });

        mEtDeviceId = view.findViewById(R.id.et_new_repair_device_id);
        mEtRepairContent = view.findViewById(R.id.et_repair_content);

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnSubmit.setProgress(0);
                deviceId = mEtDeviceId.getText().toString().trim();
                faultDescription = mEtRepairContent.getText().toString().trim();
                if(TextUtils.isEmpty(deviceId)){
                    Toast.makeText(getContext(),"请扫描设备上的二维码",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(faultDescription)){
                    Toast.makeText(getContext(),"描述不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnSubmit.setIndeterminateProgressMode(true);
                mBtnSubmit.setProgress(50);
                submitRepairForm();
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && resultCode== Activity.RESULT_OK)
        {
            deviceId = data.getStringExtra(CaptureActivity.KEY_DATA).split("#")[0];
            mEtDeviceId.setText(deviceId);
           // Toast.makeText(SpinnerActivity.this, deviceInfo, Toast.LENGTH_SHORT).show();
        }
    }

    public void submitRepairForm(){
        String phone = Config.getCachedPhone(getContext());
        new NetConnection(Config.SERVER_URL + Config.ACTION_DEVICE_FAULT_REPORT, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:
                            mBtnSubmit.setProgress(100);
                            break;
                        default:

                            mBtnSubmit.setErrorText(jsonObject.getString(Config.RESULT_MESSAGE));
                            mBtnSubmit.setProgress(-1);
                       // Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mBtnSubmit.setErrorText("数据解析异常");
                    mBtnSubmit.setProgress(-1);
                 //   Toast.makeText(getContext(), "2", Toast.LENGTH_SHORT).show();
                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                // 未能连接服务器
                mBtnSubmit.setErrorText("未能连接服务器");
                mBtnSubmit.setProgress(-1);
               // Toast.makeText(getContext(), "3", Toast.LENGTH_SHORT).show();
            }
        }, Config.KEY_PHONE,phone, Config.KEY_DEVICE_ID, deviceId, "fault_description", spSelected + ":"+faultDescription);
    }


}
