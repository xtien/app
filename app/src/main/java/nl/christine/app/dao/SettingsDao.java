package nl.christine.app.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import nl.christine.app.model.MySettings;

@Dao
public interface SettingsDao {

    @Query("select * from MySettings where id = 0")
    public MySettings load();

    @Update
    public void update(MySettings settings);
}
