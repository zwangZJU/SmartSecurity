package com.wzlab.smartsecurity.activity.main;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.skateboard.zxinglib.CaptureActivity;
import com.wzlab.smartsecurity.R;
import com.wzlab.smartsecurity.activity.account.AccountActivity;
import com.wzlab.smartsecurity.activity.account.Config;
import com.wzlab.smartsecurity.adapter.ViewPagerAdapter;
import com.wzlab.smartsecurity.net.account.Logout;
import com.wzlab.smartsecurity.widget.BottomNavMenuBar;
import com.wzlab.smartsecurity.widget.NoScrollViewPager;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NoScrollViewPager mVpMainContainer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //透明状态栏
       // window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
       // window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
       // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("首页");
        setSupportActionBar(toolbar);



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mVpMainContainer = findViewById(R.id.vp_main_container);
        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        Fragment deviceOverviewFragment = new DeviceOverviewFragment();
        Fragment alarmFragment = new AlarmFragment();
        Fragment meFragment = new MeFragment();

        mFragmentList.add(deviceOverviewFragment);
        mFragmentList.add(alarmFragment);
        mFragmentList.add(meFragment);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mVpMainContainer.setAdapter(mViewPagerAdapter);




        //设置底部的导航菜单
        int[] iconNormal = {R.drawable.ic_bottom_nav_bar_home,R.drawable.ic_bottom_nav_bar_alarm,R.drawable.ic_bottom_nav_bar_me};
        int[] iconFocus = {R.drawable.ic_bottom_nav_bar_home_focus,R.drawable.ic_bottom_nav_bar_alarm_focus,R.drawable.ic_bottom_nav_bar_me_focus};
        final String[] text = {"首页","报警","我的"};
        BottomNavMenuBar mBottomNavMenuBar = findViewById(R.id.bottom_nav_menu_bar);
        mBottomNavMenuBar.setIconRes(iconNormal)
                .setIconResSelected(iconFocus)
                .setTextRes(text)
                .setSelected(0);
        mBottomNavMenuBar.setOnItemSelectedListener(new BottomNavMenuBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                mVpMainContainer.setCurrentItem(position);
                toolbar.setTitle(text[position]);

            }
        });

        mBottomNavMenuBar.setOnItemReSelectedListener(new BottomNavMenuBar.OnItemReSelectedListener() {
            @Override
            public void onItemReSelected(int position) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_add_device){
            Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
            //跳转到扫描二维码页面
            startActivityForResult(intent,1001);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && resultCode== Activity.RESULT_OK)
        {
            String phone = Config.getCachedPhone(getApplicationContext());
            String deviceInfo=data.getStringExtra(CaptureActivity.KEY_DATA);
            Intent intent = new Intent(MainActivity.this,SelectLocationActivity.class);
            intent.putExtra("deviceInfo", deviceInfo);
            startActivity(intent);
            finish();

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        }  else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout){
            String token = Config.getCachedToken(getApplicationContext());
            new Logout(token, new Logout.SuccessCallback() {
                @Override
                public void onSuccess(String msg) {
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                }
            }, new Logout.FailCallback() {
                @Override
                public void onFail(String msg) {
                   // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                }
            });
            // 清除缓存
            Config.clearCache(getApplicationContext());
            startActivity(new Intent(MainActivity.this, AccountActivity.class));

        } else if (id == R.id.nav_shut_down) {
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
