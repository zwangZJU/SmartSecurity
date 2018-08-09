package com.wzlab.smartsecurity.activity.repair;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.LoginFragment;
import com.wzlab.smartsecurity.activity.account.RegisterFragment;
import com.wzlab.smartsecurity.activity.repair.NewRepairOrderFragment;
import com.wzlab.smartsecurity.activity.repair.RepairProcessFragment;
import com.wzlab.smartsecurity.activity.repair.RepairRecordFragment;
import com.wzlab.smartsecurity.adapter.ViewPagerAdapter;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;


public class DeviceFaultReportFragment extends Fragment {


    private TabLayout mTabLayout;
    private NoScrollViewPager mViewPager;

    public DeviceFaultReportFragment() {
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
        return inflater.inflate(R.layout.fragment_device_fault_report, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar_repair);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mTabLayout = view.findViewById(R.id.tl_repair_info);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {

            }

            @Override
            public void onTabUnselected(Tab tab) {

            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });

        // 与ViewPager关联
        mViewPager = view.findViewById(R.id.vp_repair_info);
        mTabLayout.setupWithViewPager(mViewPager);

        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        Fragment newRepairOrderFragment = new NewRepairOrderFragment();
        Fragment repairProcessFragment = new RepairProcessFragment(mViewPager);
        Fragment repairRecordFragment = new RepairRecordFragment();

        mFragmentList.add(repairProcessFragment);
        mFragmentList.add(newRepairOrderFragment);
        mFragmentList.add(repairRecordFragment);
        FragmentManager fragmentManager = getChildFragmentManager();
        ViewPagerAdapter adapter = new ViewPagerAdapter(fragmentManager, mFragmentList);
        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                 mViewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        String[] titles = new String[]{"报修进度", "新建报修单", "报修日志"};

        for(int i=0;i<titles.length;i++){
            mTabLayout.getTabAt(i).setText(titles[i]);
        }


    }
}
