/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import android.app.Application;
import androidx.lifecycle.LiveData;
import nl.christine.app.dao.SettingsDao;
import nl.christine.app.model.MySettings;

public class SettingsRepository {

    private SettingsDao settingsDao;
    private LiveData<MySettings> settings;

    public SettingsRepository(Application application) {
        SettingsDatabase db = SettingsDatabase.getDatabase(application);
        settingsDao = db.settingsDao();
        settings = settingsDao.getSettings();
    }

    public LiveData<MySettings> getSettings(){
        return settings;
    }

    public void update(final MySettings settings){
        SettingsDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.update(settings);
        });
    }

    public void setPeripheral(boolean on) {
        SettingsDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setPeripheral(on);
        });
    }

    public void setDiscovering(boolean on) {
        SettingsDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setDiscover(on);
        });
     }
}
