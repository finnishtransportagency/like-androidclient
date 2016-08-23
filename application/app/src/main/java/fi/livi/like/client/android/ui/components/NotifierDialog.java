package fi.livi.like.client.android.ui.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class NotifierDialog extends BaseDialog {

    private static final String CLOSABLE = "closable";
    private Listener clickListener;

    public interface Listener {
        void onClickNotifierDialog(DialogInterface dialog, int which, String dialogTag);
    }

    public static NotifierDialog newInstance(
            String title, String text, boolean closable, Listener clickListener) {
        NotifierDialog notifierDialog = new NotifierDialog();
        notifierDialog.initDialog(title, text);
        notifierDialog.clickListener = clickListener;
        notifierDialog.setCancelable(closable);
        Bundle moreArgs = notifierDialog.getArguments();
        moreArgs.putBoolean(CLOSABLE, closable);
        return notifierDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final boolean closable = getArguments().getBoolean(CLOSABLE);
        AlertDialog.Builder builder = getBaseBuilder(savedInstanceState);
        if (closable) {
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // this should not be unset, crash to catch problems quickly!
                    clickListener.onClickNotifierDialog(dialog, which, getTag());
                }
            });
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        clickListener = (Listener)activity;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        clickListener.onClickNotifierDialog(dialog, DialogInterface.BUTTON_NEGATIVE, getTag());
    }
}
