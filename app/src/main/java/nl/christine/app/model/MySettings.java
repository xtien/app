package nl.christine.app.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings_table")
public class MySettings {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "peripheral")
    private boolean isPeripheral = false;

    @ColumnInfo(name = "discovering")
    private boolean isDiscovering = false;

    @ColumnInfo(name = "advertisemode")
    private int advertiseMode = 0;

    @ColumnInfo(name = "signalstrength")
    private int signalStrength = 0;

    @ColumnInfo(name = "uuid")
    private String uuid;

    public boolean isPeripheral() {
        return isPeripheral;
    }

    public void setPeripheral(boolean peripheral) {
        isPeripheral = peripheral;
    }

    public boolean isDiscovering() {
        return isDiscovering;
    }

    public void setDiscovering(boolean discovering) {
        isDiscovering = discovering;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAdvertiseMode() {
        return advertiseMode;
    }

    public void setAdvertiseMode(int advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }
}
