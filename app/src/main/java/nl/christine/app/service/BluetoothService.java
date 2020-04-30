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
import nl.christine.app.model.Contact;
import nl.christine.app.model.MySettings;

import java.io.ObjectOutputStream;
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
    private String uuidString = null;
    private String serviceUUIDString = "00001810-0000-1000-8000-00805f9b34fb";
    private String serviceDataUUIDString = "00002a00-0000-1000-8000-00805f9b34fb";

    private BluetoothLeScanner scanner;
    private SettingsRepository repository;
    private Observer<MySettings> observer;

    private Queue<Runnable> commandQueue = new LinkedList<>();
    private boolean commandQueueBusy = false;
    private boolean advertising = false;
    private Map<String, Contact> contacts = new HashMap<>();

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
                                            log(LOGTAG, "discoverServices failed to start");
                                        }
                                    };
                                    handler.postDelayed(discoverServicesRunnable, delay);
                                    break;

                                case BOND_BONDING:
                                    break;
                            }

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
                            break;

                        case BluetoothProfile.STATE_DISCONNECTING:
                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:
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

                List<BluetoothGattService> services = gatt.getServices();
                log(LOGTAG, "getServices " + (services != null ? services.size() : "null"));
                if (services != null && services.size() > 0) {
                    for (BluetoothGattService service : services) {
                        log(LOGTAG, "service: " + service.getUuid() + " " + service.getType());
                        if (service.getCharacteristics() != null && service.getCharacteristics().size() > 0) {
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                log(LOGTAG, "characteristic: " + characteristic.getUuid().toString());
                                boolean c = readCharacteristic(gatt, characteristic);
                            }
                        }
                    }
                }

            } else {
                gatt.disconnect();
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == GATT_SUCCESS) {
                String value = characteristic.getStringValue(0);
                log(LOGTAG, "onCharacteristicRead: " + " " + value);

            } else {
                log(LOGTAG, String.format(Locale.ENGLISH, "ERROR: Read failed for characteristic: %s, status %d", characteristic.getUuid(), status));
                completedCommand(gatt);
            }
        }
    };

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            log(LOGTAG, "advertising onStartSuccess " + uuidString);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            log("BLE", "Advertising onStartFailure: " + errorCode);
        }
    };

    @Override
    public void onCreate() {

        // https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02

        SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        uuidString = prefs.getString("uuid", null);
        if (uuidString == null) {
            uuidString = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("uuid", uuidString);
            editor.commit();
        }

        repository = new SettingsRepository(getApplication());

        observer = settings -> {

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
                if (!advertising) {
                    advertise();
                }
            } else {
                if (advertising) {
                    stopAdvertising();
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
    }

    private void stopAdvertising() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        advertiser.stopAdvertising(advertisingCallback);
        advertising = false;
    }

    private void advertise() {
        advertising = true;
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(180000)
                .build();

        ParcelUuid uuid = new ParcelUuid(UUID.fromString(uuidString));
        ParcelUuid serviceUUID = new ParcelUuid(UUID.fromString(serviceUUIDString));
        ParcelUuid serviceDataUUID = new ParcelUuid(UUID.fromString(serviceDataUUIDString));
        byte[] d = {0x23, 0x23};

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(serviceUUID)
                .addServiceData(serviceDataUUID, uuidString.replace("-", "").substring(0, 17).getBytes())
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

    private ScanCallback leScanCallback =

            new ScanCallback() {


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

                    Map<ParcelUuid, byte[]> map = result.getScanRecord().getServiceData();
                    byte[] data = result.getScanRecord().getServiceData(ParcelUuid.fromString(serviceDataUUIDString));
                    if (data != null) {
                        String id = new String(data);
                        displayContact(id, scanRecord.getTxPowerLevel());
                    }

                    if (scanRecord != null) {
                        //stopScanning();

                        //log(LOGTAG, "BT LE device " + result.getDevice().getAddress());
                        //result.getDevice().connectGatt(getApplicationContext(), false, gattCallback, TRANSPORT_LE);
                    }
                }
            };

    private void displayContact(String id, int txPowerLevel) {

        Contact existingContact = contacts.get(id);
        if (existingContact == null) {
            contacts.put(id, new Contact(id, txPowerLevel, System.currentTimeMillis()));
            log(LOGTAG, "id: " + id + " power level " + txPowerLevel);
        } else {
            if (txPowerLevel > existingContact.getPowerLevel()) {
                existingContact.setPowerLevel(txPowerLevel);
                existingContact.plusplus();
                log(LOGTAG, "id: " + id + " power " + txPowerLevel);
            }
        }
    }

    private void stopScanning() {
        scanLeDevice(false);
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
        boolean result = commandQueue.add(() -> {
            if (!bluetoothGatt.readCharacteristic(characteristic)) {
                Log.e(LOGTAG, String.format("ERROR: readCharacteristic failed for characteristic: %s", characteristic.getUuid()));
                completedCommand(bluetoothGatt);
            } else {
                Log.d(LOGTAG, String.format("reading characteristic <%s>", characteristic.getUuid()));
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
        commandQueue.poll();
        nextCommand(bluetoothGatt);
    }
}
