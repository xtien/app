/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import nl.christine.app.dao.SettingsDao;
import nl.christine.app.model.MySettings;

@Database(entities = {MySettings.class}, views = {SettingsView.class},
        version = 1)

public abstract class AppDatabase extends RoomDatabase {
    public abstract SettingsDao settingsDao();
}
