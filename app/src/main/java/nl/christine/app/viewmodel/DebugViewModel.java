package nl.christine.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import nl.christine.app.model.MyDebug;

public class DebugViewModel extends ViewModel {

    private MutableLiveData<MyDebug> debug;

    public LiveData<MyDebug> getDebug(){
        if(debug == null){
            loadDebug();
        }
        return debug;
    }

    private void loadDebug() {

    }
}
