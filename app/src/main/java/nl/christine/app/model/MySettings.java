/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings_table")
public class MySettings {

    @PrimaryKey
    private int id;

    @ColumnInfo(name="settingsid")
    private int settingsId;

    @ColumnInfo(name = "peripheral")
    private boolean isPeripheral = false;

    @ColumnInfo(name = "discovering")
    private boolean isDiscovering = false;

    @ColumnInfo(name = "advertisemode")
    private int advertiseMode = 0;

    @ColumnInfo(name = "signalstrength")
    private int signalStrength = 0;

    @ColumnInfo(name = "timewindow")
    private long timewindow = 0l;

    @ColumnInfo(name = "uuid")
    private String uuid;

    @ColumnInfo(name = "strengthcutoff")
    private int strengthCutoff = 0;

    @ColumnInfo(name = "contactscutoff")
    private int contactsCutoff = 0;

    public boolean isPeripheral() {
        return isPeripheral;
    }

    public void setPeripheral(boolean peripheral) {
        isPeripheral = peripheral;
    }

    public boolean isDiscovering() {
        return isDiscovering;
    }

    public void setDiscovering(boolean discovering) {
        isDiscovering = discovering;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAdvertiseMode() {
        return advertiseMode;
    }

    public void setAdvertiseMode(int advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public long getTimewindow() {
        return timewindow;
    }

    public void setTimewindow(long timewindow) {
        this.timewindow = timewindow;
    }

    public int getStrengthCutoff() {
        return strengthCutoff;
    }

    public void setStrengthCutoff(int strengthCutoff) {
        this.strengthCutoff = strengthCutoff;
    }

    public int getContactsCutoff() {
        return contactsCutoff;
    }

    public void setContactsCutoff(int contactsCutoff) {
        this.contactsCutoff = contactsCutoff;
    }

    public int getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(int settingsId) {
        this.settingsId = settingsId;
    }
}
