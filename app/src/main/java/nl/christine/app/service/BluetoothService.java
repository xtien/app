package nl.christine.app.service;

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
import nl.christine.app.db.SettingsRepository;
import nl.christine.app.model.MySettings;

import java.nio.charset.Charset;
import java.util.*;

import static android.bluetooth.BluetoothDevice.*;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;

/**
 * BluetoothService does everything bluetooth. It switches on at phone startup, starts discovery,
 * sets our phone to discoverable. These last two can be switched on and off in the debug fragment
 */
public class BluetoothService extends Service {

    private String LOGTAG = getClass().getSimpleName();
    private static final String CHANNEL_ID = "3000";
    private IBinder binder;
    private int NOTIFICATION = R.string.local_service_started;
    private NotificationManager notfManager;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();
    private boolean scanning = false;
    private static final long SCAN_PERIOD = 10000;
    private String serviceUUID = "00001810-0000-1000-8000-00805f9b34fb";


    private String bluetoothDeviceAddress;
    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private BluetoothLeScanner scanner;
    private SettingsRepository repository;
    private Observer<MySettings> observer;
    private ScanSettings scanSettings;

    private Queue<Runnable> commandQueue = new LinkedList<>();
    private boolean commandQueueBusy = false;
    private int nrTries = 0;
    private boolean isRetrying = false;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            BluetoothDevice device = gatt.getDevice();

            switch (status) {
                case GATT_SUCCESS:
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:

                            long delay = 0;

                            switch (device.getBondState()) {
                                case BOND_NONE:
                                case BOND_BONDED:
                                    Runnable discoverServicesRunnable = () -> {
                                        Log.d(LOGTAG, String.format(Locale.ENGLISH, "discovering services of '%s' with delay of %d ms", device.getName(), delay));
                                        boolean result = gatt.discoverServices();
                                        if (!result) {
                                            Log.e(LOGTAG, "discoverServices failed to start");
                                        }
                                    };
                                    handler.postDelayed(discoverServicesRunnable, delay);
                                    break;

                                case BOND_BONDING:
                                    break;
                            }

                            connectionState = STATE_CONNECTED;
                            log(LOGTAG, "Connected to GATT server.");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (gatt.discoverServices()) {

                                    } else {
                                        gatt.close();
                                        scanLeDevice(true);
                                    }
                                }
                            }, 1000);
                            break;

                        case BluetoothProfile.STATE_CONNECTING:
                            connectionState = STATE_DISCONNECTED;
                            //log(LOGTAG, "Connecting to GATT server.");
                            break;

                        case BluetoothProfile.STATE_DISCONNECTING:
                            connectionState = STATE_DISCONNECTED;
                            //log(LOGTAG, "Disconnecting from GATT server.");
                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:
                            connectionState = STATE_DISCONNECTED;
                            //log(LOGTAG, "Disconnected from GATT server.");
                            gatt.close();
                            break;
                    }
                    break;

                case GATT_FAILURE:
                    log(LOGTAG, "gatt failure ");
                    gatt.disconnect();
                    break;

                default:
                    log(LOGTAG, "disconnect status " + status);
                    gatt.disconnect();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            log(LOGTAG, "onServicesDiscovered: " + status);
            if (status == GATT_SUCCESS) {
                log(LOGTAG, "gatt connect " + status);
                gatt.connect();

            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == GATT_SUCCESS) {
                log(LOGTAG, "onCharacteristicRead received: " + status);

                // process

            } else {
                log(LOGTAG, String.format(Locale.ENGLISH, "ERROR: Read failed for characteristic: %s, status %d", characteristic.getUuid(), status));
                completedCommand(gatt);
            }
        }
    };

    @Override
    public void onCreate() {

        // https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02

        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        repository = new SettingsRepository(getApplication());

        observer = new Observer<MySettings>() {

            @Override
            public void onChanged(MySettings settings) {
                if (settings.isDiscovering()) {
                    if (!scanning) {
                        scanLeDevice(true);
                    }
                } else {
                    if (scanning) {
                        scanLeDevice(false);
                    }
                }

                if (settings.isPeripheral()) {
                    BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

                    AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                            .setConnectable(true)
                            .build();

                    SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    String uuidString = prefs.getString("uuid", null);
                    if (uuidString == null) {
                        uuidString = UUID.randomUUID().toString();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("uuid", uuidString);
                        editor.commit();
                    }

                    ParcelUuid uuid = new ParcelUuid(UUID.fromString(uuidString));
                    AdvertiseData data = new AdvertiseData.Builder()
                            .setIncludeDeviceName(false)
                            .addServiceUuid(uuid)
                            //.addServiceData(uuid, "Data".getBytes(Charset.forName("UTF-8")))
                            .build();

                    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {

                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            super.onStartSuccess(settingsInEffect);
                        }

                        @Override
                        public void onStartFailure(int errorCode) {
                            log("BLE", "Advertising onStartFailure: " + errorCode);
                            super.onStartFailure(errorCode);
                        }
                    };

                    advertiser.startAdvertising(advertiseSettings, data, advertisingCallback);
                }

            }
        };

        repository.getSettings().observeForever(observer);

        notfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        showPermanentNotification(R.string.local_service_started);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            log(LOGTAG, "bluetoothadapter not enabled");
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        scanLeDevice(true);
    }

    private void log(String logtag, String message) {
        Log.i(logtag, message);
        Intent icycle = new Intent();
        icycle.setAction("nl.christine.app.message");
        icycle.putExtra("message", message);
        sendBroadcast(icycle);
    }

    private ScanCallback leScanCallback =

            new ScanCallback() {


                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    log(LOGTAG, "BT LE device batch scan " + results.size());
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    stopScanning();

                    Map<ParcelUuid, byte[]> data = result.getScanRecord().getServiceData();
                    while (data.keySet().iterator().hasNext()) {
                        ParcelUuid parcelUuid = data.keySet().iterator().next();
                        log(LOGTAG, "uuid " + parcelUuid.toString());
                    }

                    //log(LOGTAG, "BT LE device " + result.getDevice().getAddress());
                    result.getDevice().connectGatt(getApplicationContext(), false, gattCallback, TRANSPORT_LE);
                }
            };

    private void stopScanning() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            UUID BLP_SERVICE_UUID = UUID.fromString(serviceUUID);
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

            scanning = true;
            scanner.startScan(filters, scanSettings, leScanCallback);

        } else {
            scanning = false;
            scanner.stopScan(leScanCallback);
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
        notfManager.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        bluetoothAdapter.cancelDiscovery();
        repository.getSettings().removeObserver(observer);
    }

    private void showPermanentNotification(int text) {
        showTheNotification(text, true);
    }

    private void showTheNotification(int text, boolean isPermanent) {

        Log.d(LOGTAG, getString(text));
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this, "")
                .setSmallIcon(R.drawable.x10)  // the status icon
                .setChannelId(CHANNEL_ID)
                .setTicker("status")  // the status text
                .setOngoing(isPermanent)
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(getString(text))  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notfManager.notify(text, notification);
    }

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

    public boolean readCharacteristic(final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.e(LOGTAG, "ERROR: Gatt is 'null', ignoring read request");
            return false;
        }

        // Check if characteristic is valid
        if (characteristic == null) {
            Log.e(LOGTAG, "ERROR: Characteristic is 'null', ignoring read request");
            return false;
        }

        // Check if this characteristic actually has READ property
        if ((characteristic.getProperties() & PROPERTY_READ) == 0) {
            Log.e(LOGTAG, "ERROR: Characteristic cannot be read");
            return false;
        }

        // Enqueue the read command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothGatt.readCharacteristic(characteristic)) {
                    Log.e(LOGTAG, String.format("ERROR: readCharacteristic failed for characteristic: %s", characteristic.getUuid()));
                    completedCommand(bluetoothGatt);
                } else {
                    Log.d(LOGTAG, String.format("reading characteristic <%s>", characteristic.getUuid()));
                    nrTries++;
                }
            }
        });

        if (result) {
            nextCommand(bluetoothGatt);
        } else {
            Log.e(LOGTAG, "ERROR: Could not enqueue read characteristic command");
        }
        return result;
    }

    private void nextCommand(BluetoothGatt bluetoothGatt) {

        if (commandQueueBusy) {
            return;
        }

        // Check if we still have a valid gatt object
        if (bluetoothGatt == null) {
            Log.e(LOGTAG, String.format("ERROR: GATT is 'null' for peripheral '%s', clearing command queue", bluetoothGatt.getDevice().getAddress()));
            commandQueue.clear();
            commandQueueBusy = false;
            return;
        }

        // Execute the next command in the queue
        if (commandQueue.size() > 0) {
            final Runnable bluetoothCommand = commandQueue.peek();
            commandQueueBusy = true;
            nrTries = 0;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothCommand.run();
                    } catch (Exception ex) {
                        Log.e(LOGTAG, String.format("ERROR: Command exception for device '%s'", bluetoothGatt.getDevice().getName()), ex);
                    }
                }
            });
        }
    }

    private void completedCommand(BluetoothGatt bluetoothGatt) {
        commandQueueBusy = false;
        isRetrying = false;
        commandQueue.poll();
        nextCommand(bluetoothGatt);
    }
}
