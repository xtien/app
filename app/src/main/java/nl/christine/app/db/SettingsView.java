/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import androidx.room.DatabaseView;

@DatabaseView("SELECT id, peripheral, discovering FROM mysettings ")
public class SettingsView {

    public boolean discovering;
    public boolean peripheral;
}
