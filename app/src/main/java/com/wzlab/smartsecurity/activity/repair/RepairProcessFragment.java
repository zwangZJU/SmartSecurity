package com.wzlab.smartsecurity.activity.repair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baoyachi.stepview.HorizontalStepView;
import com.baoyachi.stepview.VerticalStepView;
import com.baoyachi.stepview.bean.StepBean;
import com.skateboard.zxinglib.CaptureActivity;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.net.main.RepairInfo;
import com.wzlab.smartsecurity.widget.LoadingLayout;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class RepairProcessFragment extends Fragment {

    private static final int KEY_LOADING_ERROR = -1;
    private static final int KEY_LOADING_EMPTY = 0;
    private static final int KEY_LOADING_SUCCESS = 1;
    private static final int KEY_LOADING_LOADING = 2;

    private HorizontalStepView stepViewHorizontal;
    private VerticalStepView stepViewVertical;
    private LoadingLayout loadingLayout;
    private NoScrollViewPager viewPager;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == KEY_LOADING_EMPTY){
                loadingLayout.showEmpty();
                loadingLayout.setOnEmptyButtonClickListener(new LoadingLayout.OnEmptyButtonClickListener() {
                    @Override
                    public void onClick() {
                        //  跳转到新建报修单页
                        viewPager.setCurrentItem(1);
                    }
                });


            }else if(msg.what == KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initData();
                    }
                });

            }else if(msg.what == KEY_LOADING_SUCCESS){
                stepViewHorizontal.setStepViewTexts(stepHorList);//总步骤
                stepViewVertical.setStepsViewIndicatorComplectingPosition(stepVerList.size()-2)//设置完成的步数
                        .setStepViewTexts(stepVerList);
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.content_background));
                loadingLayout.showContent();
            }
        }
    };
    private List<StepBean> stepHorList;
    private List<String> stepVerList;

    public RepairProcessFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ValidFragment")
    public RepairProcessFragment(NoScrollViewPager viewPager){
        this.viewPager = viewPager;

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repair_process, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingLayout = view.findViewById(R.id.loading_layout_repair_process);
        loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));



        // 水平
        stepViewHorizontal = view.findViewById(R.id.step_view_repair_process);
        stepViewHorizontal.setTextSize(14)//set textSize

                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.uncompleted_text_color))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsView text完成线的颜色
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.uncompleted_text_color))//设置StepsView text未完成线的颜色
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle))//设置StepsViewIndicator CompleteIcon
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked))//设置StepsViewIndicator DefaultIcon
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle_green));//设置StepsViewIndicator AttentionIcon

         //竖直
        stepViewVertical = view.findViewById(R.id.step_view_state_info);
        stepViewVertical.reverseDraw(false)//default is true
                .setLinePaddingProportion(0.85f)//设置indicator线与线间距的比例系数
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.transparent))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsView text完成线的颜色
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.transparent))//设置StepsView text未完成线的颜色
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked))//设置StepsViewIndicator CompleteIcon
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked_blue))//设置StepsViewIndicator DefaultIcon
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle_green));//设置StepsViewIndicator AttentionIcon

        initData();
    }

    public void initData(){

        // 水平
        stepHorList = new ArrayList<>();
        StepBean stepBean0 = new StepBean("提交",1);
        StepBean stepBean1 = new StepBean("接单",1);
        StepBean stepBean2 = new StepBean("维修",1);
        StepBean stepBean3 = new StepBean("完成",1);

        stepHorList.add(stepBean0);
        stepHorList.add(stepBean1);
        stepHorList.add(stepBean2);
        stepHorList.add(stepBean3);


        //竖直
        stepVerList = new ArrayList<>();

        String phone = Config.getCachedPhone(getContext());
        RepairInfo.getRepairPreogressInfo(phone, new RepairInfo.SuccessCallback() {
            @Override
            public void onSuccess(String processingState, String stateInfo) {
                if(TextUtils.isEmpty(processingState) || TextUtils.isEmpty(stateInfo)){
                    Message message = new Message();
                    message.what = KEY_LOADING_EMPTY;
                    handler.sendMessage(message);
                }else{

                    int state = Integer.parseInt(processingState);

                    for(int i=0;i<3;i++){
                        // 设置水平显示的进度
                        if(i == state){
                            stepHorList.get(i).setState(0);
                        }else if(i > state){
                            stepHorList.get(i).setState(-1);
                        }


                    }

                    //加一项防止最底部显示不完整

                    // 设置竖直显示的进度
                    String[] info = stateInfo.split("#");
                    for(int i=0;i<=state;i++){
                        String[] detail = info[i].split("%");
                        stepVerList.add(detail[1]+"\n"+detail[0]);
                    }
                    stepVerList.add(" ");
                    Message message = new Message();
                    message.what = KEY_LOADING_SUCCESS;
                    handler.sendMessage(message);


                }
            }
        }, new RepairInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message message = new Message();
                message.what = KEY_LOADING_ERROR;
                handler.sendMessage(message);
                loadingLayout.setErrorText(msg);
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
        



    }
}
