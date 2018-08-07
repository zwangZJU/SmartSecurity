package com.wzlab.smartsecurity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wzlab.smartsecurity.activity.main.DeviceOverviewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wzlab on 2018/7/10.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Fragment> mFragmentList;

    public ViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
        super(fm);
        mFragmentList = fragmentList;
    }



//    public ViewPagerAdapter(FragmentManager childFragmentManager, ArrayList<Fragment> fragmentList) {
//        super(childFragmentManager);
//        mFragmentList = fragmentList;
//    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


}
