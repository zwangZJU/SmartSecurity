package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.net.main.GetDeviceInfo;
import com.wzlab.smartsecurity.po.Device;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/7/10.
 */

public class DeviceOverviewAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Device> deviceList;
    private int ITEM_VIEW_TYPE_DATA = 0;
    private int ITEM_VIEW_TYPE_ADD = 1;

    public DeviceOverviewAdapter(Context context, ArrayList<Device> deviceList){
        this.context = context;
        this.deviceList = deviceList;
    }

    // item的点击事件
    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    // switch的点击事件
    public interface OnSwitchClickListener{
        public void onSwitchClick(View view, int position);
    }

    // deviceIcon (ImageView) 的点击事件
    public interface OnIconClickListener{
        public void onIconClick(View view, int position);
    }

    // 添加摄像头的LinearLayout添加点击事件
    public interface OnLinearLayoutClickListener{
        public void onLinearLayoutClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    private OnSwitchClickListener onSwitchClickListener;
    public void setOnSwitchClickListener(OnSwitchClickListener onSwitchClickListener){
        this.onSwitchClickListener = onSwitchClickListener;
    }

    private OnIconClickListener onIconClickListener;
    public void setOnIconClickListener(OnIconClickListener onIconClickListener){
        this.onIconClickListener = onIconClickListener;
    }

    private OnLinearLayoutClickListener onLinearLayoutClickListener;
    public void setOnLinearLayoutClickListener(OnLinearLayoutClickListener onLinearLayoutClickListener){
        this.onLinearLayoutClickListener = onLinearLayoutClickListener;
    }

    @Override
    public DeviceOverviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == ITEM_VIEW_TYPE_DATA){
            return new DeviceOverviewViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_device_info,parent,false));
        }else {
            return new DeviceOverviewViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_device_add,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final DeviceOverviewViewHolder holder = (DeviceOverviewViewHolder) viewHolder;
        holder.itemView.setTag(position);
        if(position<deviceList.size()){


            final Device device = deviceList.get(position);
            holder.mTvDeviceId.setText("ID:"+ device.getDevice_id());


            if(device.getArrange_withdraw().equals("1")){
                holder.mSwitchDefensiveState.setChecked(true);
            }else {
                holder.mSwitchDefensiveState.setChecked(false);
            }

            holder.mSwitchDefensiveState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onSwitchClickListener != null){
                        String cmd = null;
                        if(holder.mSwitchDefensiveState.isChecked()){
                            cmd = "5";
                        }else{
                            cmd = "4";
                        }


                        Log.e("ssss", "onClick: " + cmd);
                        GetDeviceInfo.deployDefense(device.getDevice_id(), cmd, new GetDeviceInfo.SuccessCallbackForDeploy() {
                            @Override
                            public void onSuccess(String deployStatus, String msg) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                if(deployStatus.equals("1")){
                                    holder.mSwitchDefensiveState.setChecked(true);
                                }else {
                                    holder.mSwitchDefensiveState.setChecked(false);
                                }
                            }
                        }, new GetDeviceInfo.FailCallback() {
                            @Override
                            public void onFail(String msg) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                        onSwitchClickListener.onSwitchClick(view, position);
                    }
                }
            });

            holder.mIvDeviceIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onIconClickListener != null){

                        onIconClickListener.onIconClick(view, position);
                    }
                }
            });

            String isAlarming = device.getIs_alarming();
            if(isAlarming.equals("1")){
                holder.mIvAlarmState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bottom_nav_bar_alarm_enable));
            }else {
                holder.mIvAlarmState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bottom_nav_bar_alarm));
            }


            holder.mLlAddCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onLinearLayoutClickListener != null){
                        onLinearLayoutClickListener.onLinearLayoutClick(view, position);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return deviceList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position != deviceList.size()){
            return ITEM_VIEW_TYPE_DATA;
        }else {
            return ITEM_VIEW_TYPE_ADD;
        }
    }

    class DeviceOverviewViewHolder extends RecyclerView.ViewHolder{

        ImageView mIvDeviceIcon;
        TextView mTvDeviceId;
        Switch mSwitchDefensiveState;
        ImageView mIvAlarmState;
        FloatingActionButton fabAddDevice;
        LinearLayout mLlAddCamera;
        public DeviceOverviewViewHolder(final View itemView) {
            super(itemView);
            mIvDeviceIcon = itemView.findViewById(R.id.iv_device_icon);
            mTvDeviceId = itemView.findViewById(R.id.tv_device_item_device_id);
            mSwitchDefensiveState = itemView.findViewById(R.id.switch_device_item_defensive_state);
            mIvAlarmState = itemView.findViewById(R.id.iv_device_item_alarm_state);
            fabAddDevice = itemView.findViewById(R.id.fab_add_device);
            mLlAddCamera = itemView.findViewById(R.id.ll_add_camera);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int position = (int) itemView.getTag();
                    if(onItemClickListener != null){
                        //暂时去掉item的点击事件
                       // onItemClickListener.onItemClick(view,position);
                    }
                }
            });


        }


    }
}
