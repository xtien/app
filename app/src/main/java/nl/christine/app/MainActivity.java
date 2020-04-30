/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import nl.christine.app.adapter.ScreenSlidePagerAdapter;
import nl.christine.app.fragment.DebugFragment;
import nl.christine.app.fragment.MainFragment;
import nl.christine.app.fragment.SettingsFragment;
import nl.christine.app.fragment.TraceFragment;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;

    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        viewPager = findViewById(R.id.viewpager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(MainFragment.newInstance());
        //pagerAdapter.addFragment(DebugFragment.newInstance());
        //pagerAdapter.addFragment(TraceFragment.newInstance());
       // pagerAdapter.addFragment(SettingsFragment.newInstance());
        viewPager.setAdapter(pagerAdapter);
    }
}
