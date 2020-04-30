/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.db;

import android.app.Application;
import androidx.lifecycle.LiveData;
import nl.christine.app.dao.ContactDao;
import nl.christine.app.model.Contact;

import java.util.List;

public class ContactRepository {

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
}
