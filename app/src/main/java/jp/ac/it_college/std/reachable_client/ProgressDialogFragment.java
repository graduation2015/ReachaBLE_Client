package jp.ac.it_college.std.reachable_client;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;

public class ProgressDialogFragment extends DialogFragment {

    private ProgressDialog progressDialog;
    public static final String DIALOG_FRAGMENT_TITLE = "title";
    public static final String DIALOG_FRAGMENT_MESSAGE = "message";


    public static ProgressDialogFragment newInstance(String title, String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_FRAGMENT_TITLE, title);
        bundle.putString(DIALOG_FRAGMENT_MESSAGE, message);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (progressDialog != null) {
            return progressDialog;
        }

        String title = getArguments().getString(DIALOG_FRAGMENT_TITLE);
        String message = getArguments().getString(DIALOG_FRAGMENT_MESSAGE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setCancelable(false);
        return progressDialog;
    }

}