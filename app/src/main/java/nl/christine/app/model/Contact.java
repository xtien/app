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

@Entity(tableName = "contact_table")
public class Contact {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contactId")
    private String contactId;
    @ColumnInfo(name = "powerLevel")
    private int powerLevel;
    @ColumnInfo(name = "number")
    private int number;
    @ColumnInfo(name = "time")
    private long time;
    @ColumnInfo(name = "rssi")
    private int rssi;

    public Contact() {

    }

    public Contact(String contactId, int txPowerLevel, int rssi, long currentTimeMillis) {
        this.contactId = contactId;
        this.powerLevel = txPowerLevel;
        this.rssi = rssi;
        this.time = currentTimeMillis;
        this.number = 1;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void plusplus() {
        number++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
