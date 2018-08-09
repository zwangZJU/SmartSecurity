package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.po.AlarmLog;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/8/8.
 */

public class AlarmLogAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<AlarmLog> alarmLogList;
    private OnItemClickListener onItemClickListener;


    public AlarmLogAdapter(Context context, ArrayList<AlarmLog> alarmLogList){
        this.alarmLogList = alarmLogList;
        this.context = context;
    }

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public AlarmLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlarmLogViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_alarm_log_info,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        AlarmLogViewHolder holder = (AlarmLogViewHolder)viewHolder;
        holder.itemView.setTag(position);

        AlarmLog alarmLog = alarmLogList.get(position);

        holder.mTvDate.setText(alarmLog.getAlarm_time());
    }

    @Override
    public int getItemCount() {
        return alarmLogList.size();
    }


    class AlarmLogViewHolder extends RecyclerView.ViewHolder{

        TextView mTvDate;
        public AlarmLogViewHolder(View itemView) {
            super(itemView);
            mTvDate = itemView.findViewById(R.id.tv_alarm_time);
            // TODO 这里写点击事件
        }
    }

}
