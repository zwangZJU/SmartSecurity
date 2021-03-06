package com.wzlab.smartsecurity.activity.me;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.wzlab.smartsecurity.Manifest;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.activity.main.MainActivity;
import com.wzlab.smartsecurity.activity.start.StartActivity;
import com.wzlab.smartsecurity.net.main.ImageUpload;
import com.wzlab.smartsecurity.utils.GraphProcess;

import java.io.File;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalCenterFragment extends Fragment {

    private static final String TAG = "PersonalCenterFragment";

    private ImageView mIvAvatar;
    private String phone;
    private String avatarURL;
    Bitmap scaledBitmap;

    private String avatarPath;
    private ImageView mIvBackground;

    private int KEY_IMAGE_UPLOAD_SUCCESS = 7;
    private int KEY_IMAGE_DOWNLOAD_SUCCESS = 8;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == KEY_IMAGE_UPLOAD_SUCCESS){
                showImage();

            }else if(msg.what == KEY_IMAGE_DOWNLOAD_SUCCESS){
                showImage();
            }
        }
    };

    public PersonalCenterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        phone = Config.getCachedPhone(getContext());
        // 接收传参
        if(getArguments()!=null){
            avatarURL = getArguments().getString("avatarURL");
        }

        return inflater.inflate(R.layout.fragment_personal_center, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvBackground = view.findViewById(R.id.iv_back);
        mIvAvatar = view.findViewById(R.id.iv_avatar);

        showImage();

        //获取需要被模糊的原图bitmap
        // 先判断有没有缓存



        mIvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ShowAvatarFragment();
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_main_container, fragment).commitAllowingStateLoss();
            }
        });

        ImageView mIvBack = view.findViewById(R.id.iv_personal_center_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

    }

    // 显示头像及背景
    private void showImage() {

        avatarPath = getContext().getExternalFilesDir(null).getPath()+"/avatar/"+phone+".png";
        File file = new File(avatarPath);
        // 预设图片
        scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_avatar);
        if(file.exists()){//如果有缓存，直接显示
            scaledBitmap = BitmapFactory.decodeFile(avatarPath);
        }else{//没有缓存
            if(avatarURL!=null && !avatarURL.equals("")){
                //但有链接，下载图片并存储
                new Thread(){
                    @Override
                    public void run() {
                        scaledBitmap = GraphProcess.downLoadImage(avatarURL);
                        GraphProcess.savaImage(scaledBitmap,getContext().getExternalFilesDir(null).getPath()+"/avatar",phone);
                        Message message = new Message();
                        message.what = KEY_IMAGE_DOWNLOAD_SUCCESS;
                        handler.sendMessage(message);
                    }
                }.start();

            }


        }

        // 背景虚化
        Bitmap blurBitmap = GraphProcess.toBlur(scaledBitmap, 3);
        mIvBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mIvBackground.setImageBitmap(blurBitmap);
        mIvAvatar.setImageBitmap(scaledBitmap);

    }




}
