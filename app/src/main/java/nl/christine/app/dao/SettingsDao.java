/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import nl.christine.app.model.MySettings;

@Dao
public interface SettingsDao {

    @Query("select * from settings_table where settingsid = 0")
    public LiveData<MySettings> getSettings();

    @Update
    public void update(MySettings settings);

    @Query("delete from settings_table")
    void deleteAll();

    @Insert
    public void insert(MySettings settings);

    @Query("update settings_table set peripheral = :peripheral where id = 0")
    void setPeripheral(boolean peripheral);

    @Query("update settings_table set discovering = :discovering where id = 0")
    void setDiscover(boolean discovering);

    @Query("update settings_table set advertisemode = :advertisemode where id = 0")
    void setAdvertiseMode(int advertisemode);

    @Query("update settings_table set signalstrength = :signalstrength where id = 0")
    void setSignalStrength(int signalstrength);

    @Query("select uuid from settings_table where id = 0")
    String getUUID();

    @Query("update settings_table set timewindow = :mSecs where id = 0")
    void setTimeWindow(long mSecs);

    @Query("update settings_table set contactscutoff = :numberOfContactsCutoffValue where id = 0")
    void setNumberOfContactsCutoff(int numberOfContactsCutoffValue);

    @Query("update settings_table set strengthcutoff = :cutoffStrength where id = 0")
    void setCutoffStrength(int cutoffStrength);
}
