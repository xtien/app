package nl.christine.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import nl.christine.app.dao.SettingsDao;
import nl.christine.app.model.MySettings;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<MySettings> settings;
    private SettingsDao settingsDao;

    public LiveData<MySettings> getSettings(){
        if(settings == null){
            loadSettings();
        }
        return settings;
    }

    public void loadSettings() {



    }
}
