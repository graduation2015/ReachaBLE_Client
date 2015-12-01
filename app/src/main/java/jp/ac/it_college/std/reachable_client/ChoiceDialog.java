package jp.ac.it_college.std.reachable_client;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

public abstract class ChoiceDialog extends DialogFragment {

    public static final String ITEMS = "items";
    public static final String CHECKED_ITEMS = "checkedItems";
    public static final int REQUEST_ITEMS = 0x01;
    public static final String TAG = "ChoiceDialog";

    public static ChoiceDialog newInstance(Fragment targetFragment, ChoiceDialog dialog) {
        dialog.setTargetFragment(targetFragment, REQUEST_ITEMS);
        dialog.setArguments(dialog.makeArgs(targetFragment.getActivity()));

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        return makeDialog();
    }

    protected abstract Bundle makeArgs(Context context);
    protected abstract Dialog makeDialog();
}