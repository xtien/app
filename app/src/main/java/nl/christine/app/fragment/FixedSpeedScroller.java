/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.fragment;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * https://stackoverflow.com/questions/8155257/slowing-speed-of-viewpager-controller-in-android
 */
public class FixedSpeedScroller extends Scroller {

    private int duration = 2000;

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
         super.startScroll(startX, startY, dx, dy, duration);
    }
}
