package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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


        String addr = getData(alarmLog.getUser_address().split("#")[0]);
        String deviceId = getData(alarmLog.getDevice_id());
        String type = getData(alarmLog.getType_());
        String alarmTime = getData(alarmLog.getAlarm_time());
        String reason = getData(alarmLog.getReason());

        String result = null;
        if(type.equals("1")){
            result = "&nbsp;&nbsp;&nbsp;&nbsp;您通过 " + "<font color='#141414'>" +"手机一键报警"+ " </font>功能发送了报警信号,有关人员会尽快联系您，<font color='#141414'>请保持手机畅通！</font>";
        }else {
            result = "&nbsp;&nbsp;&nbsp;&nbsp;您位于 " + "<font color='#141414'>" +addr + " </font>的编号为 <font color='#141414'>"+ deviceId + " </font>的设备发出了报警信号<br><br>" + "<font color='#141414'>"
                    +"报警方式："+"设备报警"+"<br>报警原因："+reason + "</font>";
        }

        holder.mTvDate.setText(alarmTime);

        holder.mTvAlarmContent.setText(Html.fromHtml(result));
    }

    @Override
    public int getItemCount() {
        return alarmLogList.size();
    }


    class AlarmLogViewHolder extends RecyclerView.ViewHolder{

        TextView mTvDate;
        TextView mTvAlarmContent;
        public AlarmLogViewHolder(View itemView) {
            super(itemView);
            mTvDate = itemView.findViewById(R.id.tv_alarm_time);
            mTvAlarmContent = itemView.findViewById(R.id.tv_alarm_log_content);
            // TODO 这里写点击事件
        }
    }

    public String getData(String s){
        if(s == null || s.equals("")){
            return "暂无数据";
        }else {
            return s;
        }
    }

    public ArrayList<AlarmLog> getStatusList() {
        return alarmLogList;
    }

    public void setStatusList(ArrayList<AlarmLog> statusList) {
        this.alarmLogList = statusList;
        notifyDataSetChanged();
    }

}
