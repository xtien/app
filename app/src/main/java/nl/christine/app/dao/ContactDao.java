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
import nl.christine.app.model.Contact;

import java.util.List;

@Dao
public interface ContactDao {

    @Query("select * from contact_table")
    LiveData<List<Contact>> getContacts();

    @Update
    void update(Contact contact);

    @Insert
    void create(Contact contact);

    @Query("delete from contact_table")
    void clear();

    @Query("select * from contact_table where contactId = :contactId and time > :time")
    List<Contact> getContactByContactID(String contactId, long time);
}
