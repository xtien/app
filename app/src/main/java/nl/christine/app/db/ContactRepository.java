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
import androidx.lifecycle.LiveData;
import nl.christine.app.dao.ContactDao;
import nl.christine.app.model.Contact;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public void clear() {
        contactDao.clear();
    }

    public Contact getContact(Contact existingContact, long timeWindow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _getContact(existingContact, timeWindow);
        } else {
            return __getContact(existingContact, timeWindow);
        }
        }

    private Contact __getContact(Contact existingContact, long timeWindow) {
        long t = System.currentTimeMillis() - timeWindow;
        List<Contact> contacts = contactDao.getContactByContactID(existingContact.getContactId());
        long newest = 0l;
        Contact result = null;
        for(Contact contact : contacts){
            if(contact.getTime()>newest && contact.getTime() > t){
                result = contact;
            }
        }
        return result;
    }


    @TargetApi(Build.VERSION_CODES.N)
    public Contact _getContact(Contact existingContact, long timeWindow) {
        long t = System.currentTimeMillis() - timeWindow;
        List<Contact> contacts = contactDao.getContactByContactID(existingContact.getContactId());
        return contacts.stream().max(Comparator.comparing(Contact::getTime)).filter(c -> c.getTime() > t).get();
    }
}
