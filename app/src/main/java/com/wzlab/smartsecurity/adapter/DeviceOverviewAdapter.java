package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.wzlab.smartsecurity.R;
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

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DeviceOverviewViewHolder holder = (DeviceOverviewViewHolder) viewHolder;
        holder.itemView.setTag(position);
        if(position<deviceList.size()){


            Device device = deviceList.get(position);
            holder.mTvDeviceId.setText("ID:"+ device.getDevice_id());


            if(device.getArrange_withdraw().equals("1")){
                holder.mSwitchDefensiveState.setChecked(true);
            }else {
                holder.mSwitchDefensiveState.setChecked(false);
            }
            holder.mSwitchDefensiveState.setEnabled(false);
            String isAlarming = device.getIs_alarming();
            if(isAlarming.equals("1")){
                holder.mIvAlarmState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bottom_nav_bar_alarm_enable));
            }else {
                holder.mIvAlarmState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bottom_nav_bar_alarm));
            }
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

        TextView mTvDeviceId;
        Switch mSwitchDefensiveState;
        ImageView mIvAlarmState;
        FloatingActionButton fabAddDevice;
        public DeviceOverviewViewHolder(final View itemView) {
            super(itemView);
            mTvDeviceId = itemView.findViewById(R.id.tv_device_item_device_id);
            mSwitchDefensiveState = itemView.findViewById(R.id.switch_device_item_defensive_state);
            mIvAlarmState = itemView.findViewById(R.id.iv_device_item_alarm_state);
            fabAddDevice = itemView.findViewById(R.id.fab_add_device);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int position = (int) itemView.getTag();
                    if(onItemClickListener != null){
                        onItemClickListener.onItemClick(view,position);
                    }
                }
            });
        }


    }
}
