package com.wzlab.smartsecurity.activity.account;

import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.Nullable;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.net.account.GetSmsCode;
import com.wzlab.smartsecurity.net.account.RecoverPassword;
import com.wzlab.smartsecurity.widget.CountDownButton;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgetPasswordFragment extends Fragment {

    private EditText mEtFpPhone;
    private EditText mEtFpPwd;
    private EditText mEtFpSmsCode;
    private CountDownButton mBtnFpSendSmsCode;
    private Button mBtnFpSettingPwd;
    private String mSmsSessionId;
    private SweetAlertDialog mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_password,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(final View view) {
        mDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        mDialog.setTitleText("正在修改...");
        mDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        mDialog.setCancelable(false);

        mEtFpPhone = view.findViewById(R.id.et_fp_phone);
        mEtFpPwd = view.findViewById(R.id.et_fp_password);
        mEtFpSmsCode = view.findViewById(R.id.et_fp_sms_code);
        mBtnFpSendSmsCode = view.findViewById(R.id.btn_fp_send_code);
        mBtnFpSettingPwd = view.findViewById(R.id.btn_setting_pwd);

        mBtnFpSendSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mEtFpPhone.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(view.getContext(),R.string.phone_number_can_not_be_empty,Toast.LENGTH_SHORT).show();

                    mBtnFpSendSmsCode.shutdown();
                    return;
                }
                mBtnFpSendSmsCode.setIsCountDown(true);
                new GetSmsCode(phone, Config.TYPE_SMS_CODE_FORGET_PASSWORD, new GetSmsCode.SuccessCallback() {
                    @Override
                    public void onSuccess(String smsSessionId, String msg) {
                        mSmsSessionId = smsSessionId;
                        Toast.makeText(view.getContext(),msg,Toast.LENGTH_SHORT).show();
                    }
                }, new GetSmsCode.FailCallback() {
                    @Override
                    public void onFail(String msg) {
                        Toast.makeText(view.getContext(),msg,Toast.LENGTH_SHORT).show();
                        mBtnFpSendSmsCode.shutdown();
                    }
                });
            }
        });

        mBtnFpSettingPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mEtFpPhone.getText().toString().trim();
                String pwd = mEtFpPwd.getText().toString().trim();
                String smsCode = mEtFpSmsCode.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(view.getContext(),R.string.phone_number_can_not_be_empty,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(view.getContext(),R.string.password_can_not_be_empty,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(smsCode)){
                    Toast.makeText(view.getContext(),R.string.code_can_not_be_empty,Toast.LENGTH_SHORT).show();
                    return;
                }

                new RecoverPassword(phone, pwd, smsCode, mSmsSessionId, new RecoverPassword.SuccessCallback() {
                    @Override
                    public void onSuccess(String result) {
                       // Toast.makeText(view.getContext(), R.string.reset_password_successfully,Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("修改成功，请重新登录!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        mEtFpSmsCode.requestFocus();
                                        getFragmentManager().popBackStack();
                                        sweetAlertDialog.dismiss();
                                    }
                                })
                                .show();






                    }
                }, new RecoverPassword.FailCallback() {
                    @Override
                    public void onFail(String msg) {
                        mDialog.dismiss();
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("操作失败！")
                                .setContentText(msg)
                                .show();
                       // Toast.makeText(view.getContext(),msg,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
