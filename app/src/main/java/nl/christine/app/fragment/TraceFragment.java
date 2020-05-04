/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import nl.christine.app.R;
import nl.christine.app.adapter.ContactsAdapter;
import nl.christine.app.viewmodel.SettingsViewModel;
import nl.christine.app.viewmodel.TraceViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tracefragment shows a trace of bluetooth contacts, discoveries, interactions
 */
public class TraceFragment extends Fragment {

    private TraceViewModel contactsViewModel;
    private RecyclerView listView;
    private ContactsAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Button clearButton;
    private ExecutorService es = Executors.newCachedThreadPool();
    private SettingsViewModel settingsViewModel;
    private Spinner cutoffStrengthSpinner;
    private Spinner numberOfContactsCutoffSpinner;

    public static TraceFragment newInstance() {
        return new TraceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.trace_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listView = view.findViewById(R.id.listview);
        clearButton = view.findViewById(R.id.clearcontacts);
        cutoffStrengthSpinner = view.findViewById(R.id.cutoffsignalstrength_spinner);
        numberOfContactsCutoffSpinner = view.findViewById(R.id.numberofcontacts_spinner);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] numberOfContactsCutoffValues = getResources().getStringArray(R.array.number_of_contacts_cutoff);
        String[] cutoffStrengthValues = getResources().getStringArray(R.array.cutoff_strength);

        contactsViewModel = ViewModelProviders.of(this).get(TraceViewModel.class);
        contactsViewModel.getContacts().observe(getActivity(), contacts -> {
            adapter.setContacts(contacts);
            adapter.notifyDataSetChanged();
        });

        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(getActivity(), settings -> {
            adapter.setSettings(settings);
            adapter.notifyDataSetChanged();

            int strengthCutoff = settings.getStrengthCutoff();
            for (int i = 0; i < cutoffStrengthValues.length; i++) {
                if (strengthCutoff == Integer.parseInt(cutoffStrengthValues[i])) {
                    cutoffStrengthSpinner.setSelection(i);
                }
            }
            int numberCutoff = settings.getContactsCutoff();
            for (int i = 0; i < numberOfContactsCutoffValues.length; i++) {
                if(numberCutoff == Integer.parseInt(numberOfContactsCutoffValues[i])){
                    numberOfContactsCutoffSpinner.setSelection(i);
                }
            }

        });

        adapter = new ContactsAdapter();
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        clearButton.setOnClickListener(v -> es.execute(() -> contactsViewModel.clear()));


        numberOfContactsCutoffSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.number_of_contacts_cutoff, android.R.layout.simple_spinner_item));
        numberOfContactsCutoffSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                settingsViewModel.setNumberOfContactsCutoff(Integer.parseInt(numberOfContactsCutoffValues[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cutoffStrengthSpinner.setAdapter((ArrayAdapter.createFromResource(getActivity(), R.array.cutoff_strength, android.R.layout.simple_spinner_item)));
        cutoffStrengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsViewModel.setCutoffStrength(Integer.parseInt(cutoffStrengthValues[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
