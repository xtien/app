/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import nl.christine.app.dao.SettingsDao;
import nl.christine.app.model.MySettings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MySettings.class}, views = {SettingsView.class}, version = 1)
public abstract class SettingsDatabase extends RoomDatabase {

    private static Callback settingsDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                SettingsDao dao = INSTANCE.settingsDao();
                MySettings settings = new MySettings();
                settings.setId(0);
                dao.insert(settings);
            });
        }
    };

    public abstract SettingsDao settingsDao();

    private static volatile SettingsDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static SettingsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SettingsDatabase.class, "word_database")
                            .addCallback(settingsDatabaseCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
