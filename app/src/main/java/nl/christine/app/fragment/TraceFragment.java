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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import nl.christine.app.R;
import nl.christine.app.adapter.ContactsAdapter;
import nl.christine.app.viewmodel.TraceViewModel;

/**
 * Tracefragment shows a trace of bluetooth contacts, discoveries, interactions
 */
public class TraceFragment extends Fragment {

    private TraceViewModel viewModel;
    private RecyclerView listView;
    private ContactsAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static TraceFragment newInstance(){
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(TraceViewModel.class);
        viewModel.getContacts().observe(getActivity(), contacts -> {
            adapter.setContacts(contacts);
            adapter.notifyDataSetChanged();
        });

        adapter = new ContactsAdapter();
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
    }
}
