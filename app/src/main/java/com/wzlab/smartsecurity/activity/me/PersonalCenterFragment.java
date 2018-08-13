package com.wzlab.smartsecurity.activity.me;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
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
import com.wzlab.smartsecurity.activity.main.MainActivity;
import com.wzlab.smartsecurity.activity.start.StartActivity;
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

    public PersonalCenterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal_center, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView mIvBackground = view.findViewById(R.id.iv_back);

        //        获取需要被模糊的原图bitmap
        Resources res = getResources();
        Bitmap scaledBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_avatar);
        Bitmap blurBitmap = GraphProcess.toBlur(scaledBitmap, 3);
        mIvBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mIvBackground.setImageBitmap(blurBitmap);


        mIvAvatar = view.findViewById(R.id.iv_avatar);
        mIvAvatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar));
        mIvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                        ) {
                    //拥有权限，执行操作
                    initPictureSelector();
                }else{
                    //没有权限，向用户请求权限
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }

            }
        });


    }


    // PictureSelector的回调
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.CHOOSE_REQUEST && resultCode == Activity.RESULT_OK) {

            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            LocalMedia localMedia = selectList.get(0);
            String filePath = null;
            if(localMedia.isCut()){
                filePath  = localMedia.getCutPath();
                Log.e(TAG, filePath );
            }else if(localMedia.isCompressed()){
                filePath = localMedia.getCompressPath();
            }else {
                filePath = localMedia.getPath();
            }
            File file = new File(filePath);
            if(file.exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                
                mIvAvatar.setImageBitmap(bitmap);
                clearCache();
            }

        }
    }

    public void initPictureSelector(){
        PictureSelector.create(PersonalCenterFragment.this)
                .openGallery(PictureMimeType.ofImage())
                .minSelectNum(1)
                .imageSpanCount(4)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)// 是否可预览图片 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .sizeMultiplier(0.9f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/smartSecurityPic")// 自定义拍照保存路径,可不填
                .enableCrop(true)// 是否裁剪 true or false
                .compress(false)// 是否压缩 true or false
                .withAspectRatio(1,1)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .isGif(false)// 是否显示gif图片 true or false
                .compressSavePath("/smartSecurityPic")//压缩图片保存地址
                .freeStyleCropEnabled(false)// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(true)// 是否圆形裁剪 true or false
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .openClickSound(false)// 是否开启点击声音 true or false
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                .minimumCompressSize(400)// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .cropWH(400,400)// 裁剪宽高比，设置如果大于图片本身宽高则无效 int
                .rotateEnabled(false) // 裁剪是否可旋转图片 true or false
                .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                .isDragFrame(false)// 是否可拖动裁剪框(固定)
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public void clearCache(){
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        RxPermissions permissions = new RxPermissions(getActivity());
        permissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(getContext());
                } else {
                    Toast.makeText(getContext(),
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });

    }

}
