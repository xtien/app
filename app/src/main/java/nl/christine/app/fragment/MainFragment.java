/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import nl.christine.app.BuildConfig;
import nl.christine.app.R;
import nl.christine.app.adapter.MainAdapter;
import nl.christine.app.service.BluetoothService;
import nl.christine.app.viewmodel.SettingsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MainFragment contains the main controls of the app. These would include the controls a user in the production
 * app would have, as well as testing and debugging controls.
 */
public class MainFragment extends Fragment {

    private static final String LOGTAG = MainFragment.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final int REQUEST_ENABLE_BT = 11;

    private BluetoothService bluetoothService;
    private Switch discoverSwitch;
    private Switch peripheralSwitch;
    private Spinner signalStrengthSpinner;
    private Spinner advertiseModeSpinner;
    private RecyclerView listView;
    private TextView uuidView;
    private View clearButton;
    private Button newIDButton;
    private Spinner timewindowSpinner;
    private TextView versionView;
    private List<Long> timewindows = new ArrayList<>();

    private SettingsViewModel settingsViewModel;
    private LinearLayoutManager layoutManager;
    private MainAdapter adapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String message = bundle.getString("message");
            adapter.addMessage(message);
            adapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    };

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        uuidView = view.findViewById(R.id.uuid);
        discoverSwitch = view.findViewById(R.id.discover_switch);
        peripheralSwitch = view.findViewById(R.id.peripheral_switch);
        signalStrengthSpinner = view.findViewById(R.id.signalstrength_spinner);
        advertiseModeSpinner = view.findViewById(R.id.advertisemode_spinner);
        listView = view.findViewById(R.id.listview);
        clearButton = view.findViewById(R.id.clear);
        newIDButton = view.findViewById(R.id.new_id);
        timewindowSpinner = view.findViewById(R.id.timewindow);
        versionView = view.findViewById(R.id.version);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] timewindowValues = getResources().getStringArray(R.array.timewindow_values);
        timewindows.clear();
        for (String s : timewindowValues) {
            timewindows.add(Long.parseLong(s));
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        uuidView.setText(prefs.getString("uuid", ""));

        adapter = new MainAdapter();
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        versionView.setText(BuildConfig.VERSION_NAME);

        newIDButton.setOnClickListener(v -> {
            SharedPreferences prefs1 = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putString("uuid", UUID.randomUUID().toString().replace("-", "").substring(0, 17));
            editor.commit();
            uuidView.setText(prefs.getString("uuid", ""));
            peripheralSwitch.setChecked(false);
        });

        clearButton.setOnClickListener(v -> adapter.clear());

        discoverSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsViewModel.setDiscovering(isChecked));
        peripheralSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsViewModel.setPeripheral(isChecked);
            }
        });

        advertiseModeSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(),
                R.array.advertisemode, android.R.layout.simple_spinner_item));
        advertiseModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsViewModel.setAdvertiseMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        signalStrengthSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(),
                R.array.signalstrength, android.R.layout.simple_spinner_item));
        signalStrengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsViewModel.setSignalStrength(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timewindowSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.timewindow, android.R.layout.simple_spinner_item));
        timewindowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingsViewModel.setTimeWindow(timewindows.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(getActivity(), settings -> {
            discoverSwitch.setChecked(settings != null && settings.isDiscovering());
            peripheralSwitch.setChecked(settings != null && settings.isPeripheral());

            if (settings != null) {
                long timeWindow = settings.getTimewindow();
                for (int i = 0; i < timewindows.size(); i++) {
                    if (timewindows.get(i) == timeWindow) {
                        timewindowSpinner.setSelection(i);
                    }
                }
                advertiseModeSpinner.setSelection(settings.getAdvertiseMode());
                signalStrengthSpinner.setSelection(settings.getSignalStrength());

             }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        switchBTOn();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            doService();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("nl.christine.app.message");
        filter.addAction("nl.christine.app.message");
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
    }

    private void switchBTOn() {
        if (bluetoothAdapter == null) {
            TextView errorTextView = getActivity().findViewById(R.id.error_message);
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(R.string.no_bluetooth);
            return;
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
                Toast.makeText(getActivity().getApplicationContext(), "Bluetooth Turned ON", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doService() {
        Intent icycle = new Intent(getActivity(), BluetoothService.class);
        getActivity().bindService(icycle, connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
