package com.wzlab.smartsecurity.activity.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.adapter.ViewPagerAdapter;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceTabContainerFragment extends Fragment {


    private TabLayout mTabLayout;
    private NoScrollViewPager mViewPager;

    public DeviceTabContainerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_tab_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTabLayout = view.findViewById(R.id.tl_device_list);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager = view.findViewById(R.id.vp_device_list);
        mViewPager.setSmooth(true);
        mViewPager.setScroll(true);
        mTabLayout.setupWithViewPager(mViewPager);

        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        Fragment intelligentTerminalFragment = new IntelligentTerminalFragment();
        Fragment cameraFragment = new CameraFragment();



        mFragmentList.add(intelligentTerminalFragment);
        mFragmentList.add(cameraFragment);

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
        String[] titles = new String[]{"智能终端", "摄像设备"};

        for(int i=0;i<titles.length;i++){
            mTabLayout.getTabAt(i).setText(titles[i]);
        }


    }


}
