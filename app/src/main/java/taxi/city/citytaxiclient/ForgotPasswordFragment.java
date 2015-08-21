package taxi.city.citytaxiclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxiclient.fragments.AuthorizationFragment;
import taxi.city.citytaxiclient.networking.RestClient;

public class ForgotPasswordFragment extends Fragment {
    private static final String ARG_PHONE = "PHONE_KEY";
    private static final String ARG_EXTRA = "EXTRA_KEY";

    AppCompatSpinner spinner;
    private String phone;
    private int extraPosition;
    private MaterialEditText phoneView;

    public static ForgotPasswordFragment newInstance(String phone, int position) {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        args.putInt(ARG_EXTRA, position);
        fragment.setArguments(args);
        return fragment;
    }

    public ForgotPasswordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE);
            extraPosition = getArguments().getInt(ARG_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        phoneView = (MaterialEditText)view.findViewById(R.id.metPhoneNumber);
        phoneView.setText(phone);
        Button btnSubmit = (Button)view.findViewById(R.id.btnSubmit);

        String[] ITEMS = {"+996", "+7", "+998"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (AppCompatSpinner) view.findViewById(R.id.spCodNumber);
        spinner.setAdapter(adapter);

        //int position = spinner.getSe
        spinner.setSelection(extraPosition);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendForgotPasswordRequest(spinner.getSelectedItem().toString() + phoneView.getText().toString());
            }
        });
        return view;
    }

    private void sendForgotPasswordRequest(final String mPhone) {
        RestClient.getAccountService().forgotPasswordRequest(mPhone, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                String backStateName = getActivity().getFragmentManager().getClass().getName();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, AuthorizationFragment.newInstance(mPhone, true, null))
                        .addToBackStack(backStateName)
                        .commit();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Не удалось отправить данные на сервер", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
