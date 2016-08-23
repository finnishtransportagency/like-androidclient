package fi.livi.like.client.android.ui.components;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public abstract class BaseDialog extends DialogFragment {

    protected static final String TITLE = "title";
    protected static final String CONTENT = "content";

    protected void initDialog(String title, String text) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(CONTENT, text);
        this.setArguments(args);
        this.setRetainInstance(false);// bug http://code.google.com/p/android/issues/detail?id=17423
    }

    protected AlertDialog.Builder getBaseBuilder(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String text = getArguments().getString(CONTENT);
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(text);
    }
}
