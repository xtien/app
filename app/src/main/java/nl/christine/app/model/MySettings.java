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
}
