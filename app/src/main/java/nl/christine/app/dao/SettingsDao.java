package nl.christine.app.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import nl.christine.app.model.MySettings;

@Dao
public interface SettingsDao {

    @Query("select * from settings_table where id = 0")
    public LiveData<MySettings> getSettings();

    @Update
    public void update(MySettings settings);

    @Query("delete from settings_table")
    void deleteAll();

    @Insert
    public void insert(MySettings settings);

    @Query("update settings_table set peripheral = :peripheral where id = 0")
    void setPeripheral(boolean peripheral);

    @Query("update settings_table set discovering = :discovering where id = 0")
    void setDiscover(boolean discovering);

    @Query("select uuid from settings_table where id = 0")
    String getUUID();
}
