/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import nl.christine.app.adapter.ScreenSlidePagerAdapter;
import nl.christine.app.fragment.FixedSpeedScroller;
import nl.christine.app.fragment.HelpFragment;
import nl.christine.app.fragment.MainFragment;
import nl.christine.app.fragment.TraceFragment;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {


    private static final String LOGTAG = MainActivity.class.getSimpleName();
    private ViewPager viewPager;

    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        viewPager = findViewById(R.id.viewpager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(MainFragment.newInstance());
        pagerAdapter.addFragment(TraceFragment.newInstance());
        pagerAdapter.addFragment(HelpFragment.newInstance());
        viewPager.setAdapter(pagerAdapter);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int numberOfStarts = prefs.getInt("number_of_starts", 0);
        if(numberOfStarts < 5){

            try {
                Field scroller;
                scroller = ViewPager.class.getDeclaredField("mScroller");
                scroller.setAccessible(true);
                FixedSpeedScroller fsscroller = new FixedSpeedScroller(viewPager.getContext(), new DecelerateInterpolator());
                scroller.set(viewPager, fsscroller);
            } catch(Exception e){
                Log.e(LOGTAG, e.getClass().getCanonicalName());
            }

            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("number_of_starts",++numberOfStarts);
            edit.commit();
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                viewPager.setCurrentItem(1);
            }, 1000l);
            handler.postDelayed(() -> {
                viewPager.setCurrentItem(2);
            },2000l);
            handler.postDelayed(() -> {
                viewPager.setCurrentItem(0);
            },4000l);
        }
    }
}
