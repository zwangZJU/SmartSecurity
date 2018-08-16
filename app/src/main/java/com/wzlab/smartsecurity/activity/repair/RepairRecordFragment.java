package com.wzlab.smartsecurity.activity.repair;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baoyachi.stepview.HorizontalStepView;
import com.baoyachi.stepview.VerticalStepView;
import com.baoyachi.stepview.bean.StepBean;
import com.google.gson.Gson;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.RepairLogAdapter;
import com.wzlab.smartsecurity.net.HttpMethod;
import com.wzlab.smartsecurity.net.NetConnection;
import com.wzlab.smartsecurity.net.main.RepairInfo;
import com.wzlab.smartsecurity.po.RepairLog;
import com.wzlab.smartsecurity.widget.LoadingLayout;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RepairRecordFragment extends Fragment {

    private RecyclerView recyclerView;
    private LoadingLayout loadingLayout;
    private ArrayList<RepairLog> repairLogList;
    private String phone;
    private AlertDialog alertDialog = null;

    private HorizontalStepView stepViewHorizontal;
    private VerticalStepView stepViewVertical;
    private LoadingLayout loadingLayoutAlert;
    private NoScrollViewPager viewPager;
    private List<StepBean> stepHorList;
    private List<String> stepVerList;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == Config.KEY_LOADING_EMPTY){
                loadingLayout.setEmptyFloatButtonVisible(false);
                loadingLayout.showEmpty();



            }else if(msg.what == Config.KEY_LOADING_ERROR){
                loadingLayout.showError();
                loadingLayout.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayout.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getRepairLogList();
                    }
                });

            }else if(msg.what == Config.KEY_LOADING_SUCCESS){

                loadingLayout.showContent();
            }else if(msg.what == 100){
                stepViewHorizontal.setStepViewTexts(stepHorList);//总步骤
                stepViewVertical.setStepsViewIndicatorComplectingPosition(stepVerList.size()-2)//设置完成的步数
                        .setStepViewTexts(stepVerList);
                loadingLayoutAlert.setBackgroundColor(getResources().getColor(R.color.content_background));
                loadingLayoutAlert.showContent();
            }else if(msg.what == 99){
                loadingLayoutAlert.showError();
                loadingLayoutAlert.setBackgroundColor(getResources().getColor(R.color.background));
                loadingLayoutAlert.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initAlertDialogView();
                    }
                });

            }
        }
    };
    public RepairRecordFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repair_log, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phone = Config.getCachedPhone(getContext());
        loadingLayout = view.findViewById(R.id.loading_layout_repair_log);

        recyclerView = view.findViewById(R.id.rv_repair_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getRepairLogList();


    }

    public void getRepairLogList(){
        new NetConnection(Config.SERVER_URL + Config.ACTION_GET_REPAIR_LOG_LIST, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    switch (jsonObject.getString(Config.KEY_STATUS)){
                        case Config.RESULT_STATUS_SUCCESS:

                            //TODO
                            repairLogList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for(int i=0;i<jsonArray.length();i++){
                                repairLogList.add(gson.fromJson(jsonArray.get(i).toString(),RepairLog.class));
                            }
                            RepairLogAdapter adapter = new RepairLogAdapter(getContext(),repairLogList);
                            adapter.setOnItemClickListener(new RepairLogAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    showRepairLogDetails();
                                }
                            });
                            recyclerView.setAdapter(adapter);
                            Message message = new Message();
                            message.what = Config.KEY_LOADING_SUCCESS;
                            handler.sendMessage(message);

                            break;
                        default:
                             Message message1 = new Message();
                             message1.what = Config.KEY_LOADING_EMPTY;
                             handler.sendMessage(message1);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingLayout.setErrorText("数据解析异常");
                    Message message = new Message();
                    message.what = Config.KEY_LOADING_ERROR;
                    handler.sendMessage(message);

                }
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                loadingLayout.setErrorText("未能连接到服务器");
                Message message = new Message();
                message.what = Config.KEY_LOADING_ERROR;
                handler.sendMessage(message);
            }
        },Config.KEY_PHONE, phone);
    }

    private void showRepairLogDetails() {
        new RepairProcessFragment();
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("订单详情")
                .setIcon(getResources().getDrawable(R.drawable.ic_repair_form))
                .setPositiveButton("知道了", null)
                .setView(R.layout.fragment_repair_process)
                .setCancelable(false).create();

        alertDialog.show();
        initAlertDialogView();

    }

    private void initAlertDialogView(){
        loadingLayoutAlert = alertDialog.findViewById(R.id.loading_layout_repair_process);
        loadingLayoutAlert.setBackgroundColor(getResources().getColor(R.color.background));



        // 水平
        stepViewHorizontal = alertDialog.findViewById(R.id.step_view_repair_process);
        stepViewHorizontal.setTextSize(14)//set textSize

                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.uncompleted_text_color))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsView text完成线的颜色
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.uncompleted_text_color))//设置StepsView text未完成线的颜色
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle))//设置StepsViewIndicator CompleteIcon
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked))//设置StepsViewIndicator DefaultIcon
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle_green));//设置StepsViewIndicator AttentionIcon

        //竖直
        stepViewVertical = alertDialog.findViewById(R.id.step_view_state_info);
        stepViewVertical.reverseDraw(false)//default is true
                .setLinePaddingProportion(0.85f)//设置indicator线与线间距的比例系数
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.transparent))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(getActivity(), android.R.color.white))//设置StepsView text完成线的颜色
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.transparent))//设置StepsView text未完成线的颜色
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked))//设置StepsViewIndicator CompleteIcon
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_radio_button_checked_blue))//设置StepsViewIndicator DefaultIcon
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check_circle_green));//设置StepsViewIndicator AttentionIcon

        // 水平
        stepHorList = new ArrayList<>();
        StepBean stepBean0 = new StepBean("提交",1);
        StepBean stepBean1 = new StepBean("接单",-1);
        StepBean stepBean2 = new StepBean("维修",-1);
        StepBean stepBean3 = new StepBean("完成",-1);

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
                    message.what = Config.KEY_LOADING_EMPTY;
                    handler.sendMessage(message);
                }else{

                    int state = Integer.parseInt(processingState);

                    for(int i=0;i<3;i++){
                        // 设置水平显示的进度
                        if(i == state-1){
                            stepHorList.get(i).setState(0);
                        }else if(i >= state){
                            stepHorList.get(i).setState(-1);
                        }

                    }

                    //加一项防止最底部显示不完整

                    // 设置竖直显示的进度
                    String[] info = stateInfo.split("#");
                    for(int i=0;i<state;i++){
                        String[] detail = info[i].split("%");
                        stepVerList.add(detail[1]+"\n"+detail[0]);
                    }
                    stepVerList.add(" ");
                    Message message = new Message();
                    message.what = 100;
                    handler.sendMessage(message);


                }
            }
        }, new RepairInfo.FailCallback() {
            @Override
            public void onFail(String msg) {
                Message message = new Message();
                message.what =99;
                handler.sendMessage(message);
                loadingLayoutAlert.setErrorText(msg);
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
