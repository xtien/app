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
        AppDatabase db = AppDatabase.getDatabase(application);
        settingsDao = db.settingsDao();
        settings = settingsDao.getSettings();
    }

    public LiveData<MySettings> getSettings(){
        return settings;
    }

    public String getUUID(){
        return settingsDao.getUUID();
    }

    public void update(final MySettings settings){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.update(settings);
        });
    }

    public void setPeripheral(boolean isChecked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setPeripheral(isChecked);
        });
    }

    public void setDiscovering(boolean isChecked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setDiscover(isChecked);
        });
     }

    public void setAdvertiseMode(int mode) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setAdvertiseMode(mode);
        });
    }

    public void setSignalStrength(int strength) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setSignalStrength(strength);
        });
    }

    public void setTimeWindow(long mSecs) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setTimeWindow(mSecs);
        });
    }

    public void setNumberOfContactsCutoff(int numberOfContactsCutoffValue) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setNumberOfContactsCutoff(numberOfContactsCutoffValue);
        });
    }

    public void setCutoffStrength(int cutoffStrength) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.setCutoffStrength(cutoffStrength);
        });
    }
}
