package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wzlab.smartsecurity.R;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/7/23.
 */

public class PoiListAdapter extends RecyclerView.Adapter{

    private Context context;
    private ArrayList<PoiInfo> poiList;


    public PoiListAdapter(Context context, ArrayList<PoiInfo> poiList){
        this.context = context;
        this.poiList = poiList;
    }

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public PoiInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PoiInfoViewHolder poiInfoViewHolder = new PoiInfoViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_poi_info,parent,false));
        return poiInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        PoiInfoViewHolder holder = (PoiInfoViewHolder)viewHolder;
        holder.itemView.setTag(position);
        PoiInfo poiInfo= poiList.get(position);




        if(position == 0){
            holder.mIvPoiIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_my_location));
            holder.mIvPoiIcon.setPadding(7,7,7,7);

            holder.mTvSematicDesc.setTextColor(context.getResources().getColor(R.color.colorPrimary));


        }
        holder.mTvCompleteAddr.setText(poiInfo.address);
        holder.mTvSematicDesc.setText(poiInfo.name);



    }

    @Override
    public int getItemCount() {
        // 这里固定了个数

        return 6;
    }

    class PoiInfoViewHolder extends RecyclerView.ViewHolder{

        ImageView mIvPoiIcon;
        TextView mTvSematicDesc;
        TextView mTvCompleteAddr;
        public PoiInfoViewHolder(final View itemView) {
            super(itemView);
            mIvPoiIcon = itemView.findViewById(R.id.iv_poi_icon);
            mTvSematicDesc = itemView.findViewById(R.id.tv_sematic_description);
            mTvCompleteAddr = itemView.findViewById(R.id.tv_complete_address);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (int) itemView.getTag();
                    if(onItemClickListener!= null){
                        onItemClickListener.onItemClick(view, position);
                    }
                }
            });

        }
    }

}
