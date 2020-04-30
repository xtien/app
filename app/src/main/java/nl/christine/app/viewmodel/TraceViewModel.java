package nl.christine.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import nl.christine.app.db.ContactRepository;
import nl.christine.app.model.Contact;

import java.util.List;

public class TraceViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final LiveData<List<Contact>> contacts;

    public TraceViewModel(Application application) {
        super(application);
        repository = new ContactRepository(application);
        contacts = repository.getContacts();
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }
}
