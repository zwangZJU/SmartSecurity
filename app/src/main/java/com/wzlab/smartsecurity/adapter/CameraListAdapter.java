package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.po.Camera;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/8/23.
 */

public class CameraListAdapter extends RecyclerView.Adapter{

    private Context context;



    private ArrayList<Camera> cameraList;

    public CameraListAdapter(Context context, ArrayList<Camera> cameraList){
        this.context = context;
        this.cameraList = cameraList;

    }


    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public CameraViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CameraViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_camera_info,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CameraViewHolder holder = (CameraViewHolder) viewHolder;
        Camera camera = cameraList.get(position);

        String url = "https://statics.ys7.com/device/image/"+camera.getCamera_type().trim()+"/101.jpeg";
        holder.itemView.setTag(position);
        Glide.with(context).load(url).into(holder.mIvCameraImg);
       // holder.mIvCameraImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_avatar));

    }

    @Override
    public int getItemCount() {
        return cameraList.size();
    }


    class CameraViewHolder extends RecyclerView.ViewHolder{
        ImageView mIvCameraImg;

        public CameraViewHolder(final View itemView) {
            super(itemView);
            mIvCameraImg = itemView.findViewById(R.id.iv_camera_img);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (int) itemView.getTag();
                    if(onItemClickListener != null){
                        onItemClickListener.onItemClick(view, position);
                    }
                }
            });
        }
    }

    public ArrayList<Camera> getCameraList() {
        return cameraList;
    }

    public void setCameraList(ArrayList<Camera> cameraList) {
        this.cameraList = cameraList;
        notifyDataSetChanged();
    }
}
