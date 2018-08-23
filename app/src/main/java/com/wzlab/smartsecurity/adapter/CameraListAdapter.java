package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.wzlab.smartsecurity.R;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/8/23.
 */

public class CameraListAdapter extends RecyclerView.Adapter{

    private Context context;



    private ArrayList<EZCameraInfo> cameraList;

    public CameraListAdapter(Context context, ArrayList<EZCameraInfo> cameraList){
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
        holder.itemView.setTag(position);
        holder.mIvCameraImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_avatar));
        EZCameraInfo cameraInfo = cameraList.get(position);
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

    public ArrayList<EZCameraInfo> getCameraList() {
        return cameraList;
    }

    public void setCameraList(ArrayList<EZCameraInfo> cameraList) {
        this.cameraList = cameraList;
        notifyDataSetChanged();
    }
}
