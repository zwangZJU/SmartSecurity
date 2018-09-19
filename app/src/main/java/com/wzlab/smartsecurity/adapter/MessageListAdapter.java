package com.wzlab.smartsecurity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.po.Advert;
import com.wzlab.smartsecurity.utils.DateFormat;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by wzlab on 2018/9/18.
 */

public class MessageListAdapter extends RecyclerView.Adapter{
    Context context;
    ArrayList<Advert> advertList;

    public MessageListAdapter(Context context, ArrayList<Advert> advertList) {
        this.context = context;
        this.advertList = advertList;
    }

    public ArrayList<Advert> getAdvertList() {
        return advertList;
    }

    public void setAdvertList(ArrayList<Advert> advertList) {
        this.advertList = advertList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item_advert,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder mHolder, int position) {
        MessageViewHolder holder = (MessageViewHolder) mHolder;
        holder.itemView.setTag(position);
        Advert advert = advertList.get(position);
        String imgUrl = Config.ADVERT_IMG_URL+advert.getImg_url();
        String url = Config.ADVERT_HTML_URL+advert.getUrl();
        String title = advert.getName();
        String subtitle = advert.getShortname();
        String date = advert.getCreate_date();

        String date1 = DateFormat.ChangeFormat(date,"M月d日 kk:mm");
        Glide.with(context).load(imgUrl).into(holder.mIvImg);
        holder.mTvTitle.setText(title);
        holder.mTvSubtitle.setText(subtitle);
        holder.mTvDate.setText(date1);

    }

    @Override
    public int getItemCount() {
        return advertList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView mTvDate;
        ImageView mIvImg;
        TextView mTvTitle;
        TextView mTvSubtitle;

        public MessageViewHolder(final View itemView) {
            super(itemView);
            mTvDate = itemView.findViewById(R.id.tv_advert_date);
            mIvImg = itemView.findViewById(R.id.iv_advert_img);
            mTvTitle = itemView.findViewById(R.id.tv_advert_title);
            mTvSubtitle = itemView.findViewById(R.id.tv_advert_subtitle);

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
}
