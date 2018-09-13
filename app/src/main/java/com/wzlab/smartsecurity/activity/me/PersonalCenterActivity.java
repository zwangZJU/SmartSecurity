package com.wzlab.smartsecurity.activity.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wzlab.smartsecurity.R;

public class PersonalCenterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = new PersonalCenterFragment();

       getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_main_container, fragment).commitAllowingStateLoss();

    }
}
