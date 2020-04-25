/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import nl.christine.app.MyApplication;
import nl.christine.app.dao.SettingsDao;

import javax.inject.Singleton;

@Module
public class AppModule {

    private final AppDatabase db;

    public AppModule() {
        db = Room.databaseBuilder(MyApplication.getAppContext(), AppDatabase.class, "settings-db").build();
    }

    @Provides
    @Singleton
    public SettingsDao settings() {
        return db.settingsDao();
    }
}
