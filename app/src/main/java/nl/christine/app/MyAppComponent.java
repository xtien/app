/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app;

import android.app.Application;
import dagger.BindsInstance;
import dagger.Component;
import nl.christine.app.db.AppModule;
import nl.christine.app.fragment.MainFragment;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface MyAppComponent {

    void inject(MainActivity mainActivity);

    void inject(MainFragment mainFragment);

    @Component.Builder
    interface Builder {
        MyAppComponent build();

        @BindsInstance
        Builder application(Application application);
    }
}
