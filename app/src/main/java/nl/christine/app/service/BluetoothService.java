/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.service;

import android.annotation.TargetApi;
import android.app.*;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import nl.christine.app.MainActivity;
import nl.christine.app.R;
import nl.christine.app.db.ContactRepository;
import nl.christine.app.db.SettingsRepository;
import nl.christine.app.model.Contact;
import nl.christine.app.model.MySettings;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BluetoothService does everything bluetooth. It switches on at phone startup, starts discovery,
 * sets our phone to discoverable. These last two can be switched on and off in the debug fragment
 */
public class BluetoothService extends Service {

    private String LOGTAG = getClass().getSimpleName();
    private static final String CHANNEL_ID = "3000";
    private IBinder binder;
    private NotificationManager notfManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning = false;
    private boolean advertising = false;
    private String uuidString = null;
    private String serviceUUIDString = "00001830-0000-1000-8000-00805f9b34fb";
    private String serviceDataUUIDString = "00002a00-0000-1000-8000-00805f9b34fb";

    private BluetoothLeScanner scanner;
    private SettingsRepository repository;
    private Observer<MySettings> observer;

    private Map<String, Contact> contacts = new HashMap<>();
    private ContactRepository contactRepository;
    private int notificationId = 37;
    private int advertiseMode = 0;
    private int signalStrength = 0;

    private ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
    private long timeWindow = 60000;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            log(LOGTAG, "advertising onStartSuccess " + uuidString);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!scanning) {
                    showPermanentNotification(R.string.local_service_started);
                }
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            log("BLE", "Advertising onStartFailure: " + errorCode);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!scanning) {
                    notfManager.cancel(notificationId);
                }
            }
        }
    };

    private ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            log(LOGTAG, "BT LE device batch scan " + results.size());
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            ScanRecord scanRecord = result.getScanRecord();

            if (scanRecord.getDeviceName() != null) {
                log(LOGTAG, "BT LE device scan " + scanRecord.getDeviceName());
            }

            byte[] data = result.getScanRecord().getServiceData(ParcelUuid.fromString(serviceDataUUIDString));
            if (data != null) {
                String id = new String(data);
                displayContact(id, scanRecord.getTxPowerLevel());
            }
        }
    };

    @Override
    public void onCreate() {

        contactRepository = new ContactRepository(getApplication());

        SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        uuidString = prefs.getString("uuid", null);
        if (uuidString == null) {
            uuidString = UUID.randomUUID().toString().replace("-", "").substring(0, 17);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("uuid", uuidString);
            editor.commit();
        }

        repository = new SettingsRepository(getApplication());

        observer = settings -> {

            advertiseMode = settings.getAdvertiseMode();
            signalStrength = settings.getSignalStrength();
            timeWindow = settings.getTimewindow();

            if (settings.isDiscovering()) {
                if (!scanning) {
                    BluetoothService.this.scanLeDevice(true);
                }
            } else {
                if (scanning) {
                    BluetoothService.this.scanLeDevice(false);
                }
            }

            boolean advertisingHandled = false;
            if (settings.isPeripheral()) {
                if (!advertising) {
                    advertisingHandled = true;
                    BluetoothService.this.advertise();
                }
            } else {
                if (advertising) {
                    advertisingHandled = true;
                    BluetoothService.this.stopAdvertising();
                }
            }

            if (!advertisingHandled && advertising) {
                es.execute(() -> BluetoothService.this.stopAdvertising());
                es.schedule(() -> BluetoothService.this.advertise(), 100l, TimeUnit.MILLISECONDS);
            }
        };

        repository.getSettings().observeForever(observer);

        notfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            log(LOGTAG, "bluetoothadapter not enabled");
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    private void stopAdvertising() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        advertiser.stopAdvertising(advertisingCallback);
        advertising = false;
        if (!scanning) {
            notfManager.cancel(notificationId);
        }
    }

    private void advertise() {
        uuidString = getSharedPreferences("prefs", MODE_PRIVATE).getString("uuid", "123");
        advertising = true;
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        if (advertiser == null) {
            log(LOGTAG, "No bluetooth LE advertiser");
            return;
        }

        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(advertiseMode)
                .setTxPowerLevel(signalStrength)
                .setConnectable(true)
                .setTimeout(180000)
                .build();

        ParcelUuid serviceUUID = new ParcelUuid(UUID.fromString(serviceUUIDString));
        ParcelUuid serviceDataUUID = new ParcelUuid(UUID.fromString(serviceDataUUIDString));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(serviceUUID)
                .addServiceData(serviceDataUUID, uuidString.getBytes())
                .build();

        advertiser.startAdvertising(advertiseSettings, data, advertisingCallback);
    }

    private void log(String logtag, String message) {
        Log.i(logtag, message);
        Intent icycle = new Intent();
        icycle.setAction("nl.christine.app.message");
        icycle.putExtra("message", message);
        sendBroadcast(icycle);
    }

    private void displayContact(String id, int txPowerLevel) {

        es.execute(() -> {
            Contact existingContact = contacts.get(id);
            if (existingContact == null) {
                Contact newContact = new Contact(id, txPowerLevel, System.currentTimeMillis());
                contactRepository.create(newContact);
                contacts.put(id, newContact);
                log(LOGTAG, "id: " + id + " power " + txPowerLevel);
            } else {
                Optional<Contact> foundContact = contactRepository.getContact(existingContact, timeWindow);
                if (foundContact.isPresent()) {
                    existingContact = foundContact.get();
                }
                existingContact.plusplus();
                if (txPowerLevel > existingContact.getPowerLevel()) {
                    existingContact.setPowerLevel(txPowerLevel);
                    log(LOGTAG, "id: " + id + " power " + txPowerLevel);
                }
                contacts.put(existingContact.getContactId(), existingContact);
                contactRepository.update(existingContact);
            }
        });
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {
            UUID BLP_SERVICE_UUID = UUID.fromString(serviceUUIDString);
            UUID[] serviceUUIDs = new UUID[]{BLP_SERVICE_UUID};
            List<ScanFilter> filters = null;
            if (serviceUUIDs != null) {
                filters = new ArrayList<>();
                for (UUID serviceUUID : serviceUUIDs) {
                    ScanFilter filter = new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(serviceUUID))
                            .build();
                    filters.add(filter);
                }
            }

            scanner.flushPendingScanResults(leScanCallback);
            scanning = true;
            scanner.startScan(leScanCallback);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!advertising) {
                    showPermanentNotification(R.string.local_service_started);
                }
            }

        } else {
            scanning = false;
            scanner.stopScan(leScanCallback);
            if (!advertising) {
                notfManager.cancel(notificationId);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        notfManager.cancel(notificationId);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        bluetoothAdapter.cancelDiscovery();
        repository.getSettings().removeObserver(observer);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void showPermanentNotification(int text) {

        Log.d(LOGTAG, getString(text));
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this, "")
                .setSmallIcon(R.drawable.lb)  // the status icon
                .setChannelId(CHANNEL_ID)
                .setTicker("status")  // the status text
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(getString(text))  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notfManager.notify(notificationId, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
