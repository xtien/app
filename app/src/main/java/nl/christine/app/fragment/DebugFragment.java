package nl.christine.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import nl.christine.app.R;
import nl.christine.app.viewmodel.DebugViewModel;

public class DebugFragment extends Fragment {

    private DebugViewModel viewModel;
    private Button sendEventButton;
    private Button sendSickButton;

    public static DebugFragment newInstance(){
        return new DebugFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.debug_fragment, container, false);
   }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(DebugViewModel.class);
        sendEventButton = getActivity().findViewById(R.id.send_event);
        sendEventButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        sendSickButton = getActivity().findViewById(R.id.send_sick);
        sendSickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }
}
