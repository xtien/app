/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.adapter;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import nl.christine.app.R;
import nl.christine.app.fragment.BaseViewHolder;
import nl.christine.app.model.Contact;
import nl.christine.app.model.MySettings;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.SimpleDateFormat;
import java.util.*;

public class ContactsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private SimpleDateFormat sFormat;
    List<Contact> contacts = new ArrayList<>();
    FastDateFormat format;
    private MySettings settings;

    public ContactsAdapter(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            format = FastDateFormat.getInstance("MM-dd kk:mm", TimeZone.getDefault(), Locale.getDefault());
        } else {
            sFormat = new SimpleDateFormat("MM-dd kk:mm");
        }
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        ContactsAdapter.ViewHolder viewHolder = new ContactsAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setSettings(MySettings settings) {
        this.settings = settings;
    }

    private class ViewHolder extends BaseViewHolder {

        TextView contactIdView;
        TextView timeView;
        TextView numberView;
        TextView powerView;
        TextView rssiView;

        public ViewHolder(View view) {
            super(view);
            contactIdView = view.findViewById(R.id.contact_id);
            timeView = view.findViewById(R.id.time);
            numberView = view.findViewById(R.id.number);
            powerView = view.findViewById(R.id.power);
            rssiView = view.findViewById(R.id.rssi);
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);
            Contact contact = contacts.get(position);
            contactIdView.setText(contact.getContactId());
            Date date = new Date(contact.getTime());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timeView.setText(format.format(date));
            } else {
                timeView.setText(sFormat.format(date));
            }
            numberView.setText(String.valueOf(contact.getNumber()));
            powerView.setText(String.valueOf(contact.getPowerLevel()));
            rssiView.setText(String.valueOf(contact.getRssi()));

            if(contact.getRssi() > settings.getStrengthCutoff() && contact.getNumber() > settings.getContactsCutoff()){
                contactIdView.setTextColor(Color.RED);
            } else {
                contactIdView.setTextColor(Color.BLACK);
            }
            if(contact.getRssi() > settings.getStrengthCutoff()){
                rssiView.setTextColor(Color.BLUE);
            } else {
                rssiView.setTextColor(Color.BLACK);
            }
            if(contact.getNumber() > settings.getContactsCutoff()){
                numberView.setTextColor(Color.BLUE);
            } else {
                numberView.setTextColor(Color.BLACK);
            }
        }

        @Override
        protected void clear() {
            contactIdView.setText("");
            timeView.setText("");
            numberView.setText("");
            powerView.setText("");
        }
    }

}
