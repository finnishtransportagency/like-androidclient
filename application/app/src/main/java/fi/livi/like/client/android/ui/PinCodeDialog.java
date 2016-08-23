package fi.livi.like.client.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import fi.livi.like.client.android.R;

public class PinCodeDialog extends DialogFragment implements View.OnClickListener, TextWatcher {

    public final static String PIN_CODE_DIALOG_TAG = "pinCodeDialog";

    private Listener pinCodeDialogListener;
    private Button okButton;
    private EditText pinCodeEditor;

    public interface Listener {
        void onPinCodeEntered(String pinCode);
    }

    public PinCodeDialog() {
        // Required empty public constructor
    }

    public static PinCodeDialog newInstance() {
        PinCodeDialog fragment = new PinCodeDialog();
        fragment.setCancelable(false);
        fragment.setRetainInstance(false);// bug http://code.google.com/p/android/issues/detail?id=17423

        return fragment;
    }

    public void setSaveButtonVisible(boolean visible) {
        okButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View dialogView = inflater.inflate(R.layout.fragment_pin_code_dialog, container, false);
        okButton = (Button)dialogView.findViewById(R.id.pin_code_dialog_ok_button);
        okButton.setOnClickListener(this);
        pinCodeEditor = (EditText) dialogView.findViewById(R.id.pin_code_input);
        pinCodeEditor.addTextChangedListener(this);
        return dialogView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            pinCodeDialogListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pinCodeDialogListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pin_code_dialog_ok_button :
                okButton.setVisibility(View.INVISIBLE);
                if (pinCodeDialogListener != null) {
                    pinCodeDialogListener.onPinCodeEntered(pinCodeEditor.getText().toString());
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        okButton.setVisibility(editable.length() >= 4 ? View.VISIBLE : View.INVISIBLE);
    }
}
