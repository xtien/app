package nl.christine.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import nl.christine.app.model.MyTrace;

public class TraceViewModel extends ViewModel {

    private MutableLiveData<MyTrace> trace;

    public LiveData<MyTrace> getTrace(){
        if(trace == null){
            loadTrace();
        }
        return trace;
    }

    private void loadTrace() {

    }
}
