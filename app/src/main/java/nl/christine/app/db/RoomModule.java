/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import android.app.Application;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import nl.christine.app.dao.SettingsDao;

import javax.inject.Singleton;

@Module
public class RoomModule {

    private AppDatabase appDatabase;

    @Singleton
    @Provides
    AppDatabase providesAppDatabase(Application application){
        return Room.databaseBuilder(application, AppDatabase.class, "app-database").build();
    }

    @Singleton
    @Provides
    SettingsDao providesSettingsDao(AppDatabase appDatabase){
        return appDatabase.settingsDao();
    }
}
