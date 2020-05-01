/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import nl.christine.app.db.ContactRepository;
import nl.christine.app.model.Contact;

import java.util.List;

public class TraceViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final LiveData<List<Contact>> contacts;

    public TraceViewModel(Application application) {
        super(application);
        repository = new ContactRepository(application);
        contacts = repository.getContacts();
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    public void clear() {
        repository.clear();
    }
}
