/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.util.Log;
import androidx.lifecycle.LiveData;
import nl.christine.app.dao.ContactDao;
import nl.christine.app.model.Contact;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ContactRepository {

    private static final String LOGTAG = ContactRepository.class.getSimpleName();
    private ContactDao contactDao;
    private LiveData<List<Contact>> contacts;


    public ContactRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        contactDao = db.contactDao();
        contacts = contactDao.getContacts();
    }

    public void update(final Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.update(contact);
        });
    }

    public void create(final Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.create(contact);
        });
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;

    }

    public void clear() {
        contactDao.clear();
    }

    public Contact getContact(String id, long timeWindow) {
        long time = System.currentTimeMillis() - timeWindow;
        List<Contact> contacts = contactDao.getContactByContactID(id, time);
        if (!contacts.isEmpty()) {
            if (contacts.size() > 1) {
                Log.e(LOGTAG, "More than one contact found, this shouldn't happen");
            }
            return contacts.get(0);
        }
        return null;
    }
}
