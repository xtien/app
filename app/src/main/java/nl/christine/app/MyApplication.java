/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    // https://proandroiddev.com/dagger-2-component-builder-1f2b91237856
    // https://medium.com/@marco_cattaneo/integrate-dagger-2-with-room-persistence-library-in-few-lines-abf48328eaeb

    private static Context applicationContext;
    private MyAppComponent component;

    public MyApplication(){
        applicationContext = getApplicationContext();
    }

    @Override public void onCreate() {
        super.onCreate();

        MyAppComponent appComponent = DaggerMyAppComponent.builder().application(this).build();
    }

    public static Context getAppContext(){
        return applicationContext;
    }
}