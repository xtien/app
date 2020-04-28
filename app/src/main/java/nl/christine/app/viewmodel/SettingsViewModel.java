package nl.christine.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import nl.christine.app.dao.SettingsDao;
import nl.christine.app.db.SettingsRepository;
import nl.christine.app.model.MySettings;

public class SettingsViewModel extends AndroidViewModel {

    private LiveData<MySettings> settings;
    private SettingsRepository repository;

    public SettingsViewModel(Application application) {
        super(application);
        repository = new SettingsRepository(application);
        settings = repository.getSettings();
    }

    public LiveData<MySettings> getSettings() {
        return settings;
    }

    public void setPeripheral(boolean on) {
        repository.setPeripheral(on);
    }

    public void setDiscovering(boolean on ){
        repository.setDiscovering(on);
    }
}
