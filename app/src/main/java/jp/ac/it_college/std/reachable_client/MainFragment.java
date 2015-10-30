package jp.ac.it_college.std.reachable_client;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ObjectListing> {
    private List<S3ObjectSummary> mObjectSummaries = new ArrayList<>();
    private ProgressDialogFragment dialogFragment;

    private static final int ITEM_DOWNLOAD = 0;
    private static final int ITEM_DELETE = 1;
    private static final String DIRECTORY_NAME = "/ReachaBLE/";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        setListAdapter(new S3ObjectsListAdapter(getActivity(), R.layout.row_s3_objects, mObjectSummaries));
        dialogFragment = ProgressDialogFragment.newInstance(
                getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_message));

        // オブジェクトリスト更新
//        reloadList();

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_s3_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
/*        switch (item.getItemId()) {
            case R.id.menu_s3_list_reload:
                reloadList();
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void reloadList() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        S3ObjectSummary summary = (S3ObjectSummary) getListAdapter().getItem(position);
        showAlertDialog(summary);
    }

    private void showAlertDialog(final S3ObjectSummary summary) {
        new AlertDialog.Builder(getActivity())
                .setTitle(summary.getKey())
                .setItems(getResources().getStringArray(R.array.mode_array),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                selectItem(summary, i);
                            }
                        })
                .show();
    }

    private void selectItem(S3ObjectSummary summary, int position) {
        switch (position) {
            case ITEM_DOWNLOAD:
                beginDownload(summary);
                break;
            case ITEM_DELETE:
                break;
        }
    }

    private String makeDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                Toast.makeText(getActivity(), "ディレクトリの作成に失敗", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return directory.getAbsolutePath();
    }

    private void beginDownload(S3ObjectSummary summary) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + DIRECTORY_NAME;
        File file = new File(makeDirectory(path), summary.getKey());

        TransferUtility utility = ((MainActivity) getActivity())
                .getClientManager().getTransferUtility(getActivity());

        TransferObserver observer = utility.download(
                Constants.BUCKET_NAME,
                summary.getKey(),
                file);

        observer.setTransferListener(new S3DownloadListener());

    }

    @Override
    public Loader<ObjectListing> onCreateLoader(int i, Bundle bundle) {
        dialogFragment.show(getFragmentManager(), "Progress");

        AmazonS3Client client = ((MainActivity) getActivity()).getClientManager().getS3Client();
        return new S3GetObjectListAsyncTaskLoader(getActivity(), client);
    }

    @Override
    public void onLoadFinished(Loader<ObjectListing> loader, ObjectListing objectListing) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dialogFragment.dismiss();
            }
        });

        mObjectSummaries.clear();
        mObjectSummaries.addAll(objectListing.getObjectSummaries());
        ((S3ObjectsListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ObjectListing> loader) {

    }

    private class S3DownloadListener implements TransferListener {
        private ProgressDialogFragment mDialogFragment;
        private static final String DIALOG_TITLE = "Download";
        private static final String DIALOG_MESSAGE = "Downloading...";
        private static final String TAG = "S3DownloadListener";


        public S3DownloadListener() {
            mDialogFragment = ProgressDialogFragment
                    .newInstance(DIALOG_TITLE, DIALOG_MESSAGE);
        }

        @Override
        public void onStateChanged(int i, TransferState transferState) {
            switch (transferState) {
                case IN_PROGRESS:
                    mDialogFragment.show(getFragmentManager(), "tag_downloading");
                    break;
                case COMPLETED:
                    mDialogFragment.dismiss();
                    Toast.makeText(getActivity(), "Download completed.", Toast.LENGTH_SHORT).show();
                    break;
                case FAILED:
                    mDialogFragment.dismiss();
                    Toast.makeText(getActivity(), "Download failed.", Toast.LENGTH_SHORT).show();
                default:
                    Log.d(TAG, transferState.name());
                    break;
            }
        }

        @Override
        public void onProgressChanged(int i, long l, long l1) {

        }

        @Override
        public void onError(int i, Exception e) {

        }
    }
/*
    private TextView mLabelAccessKey;
    private TextView mLabelSecretKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        mLabelAccessKey = (TextView) view.findViewById(R.id.lbl_access_key);
        mLabelSecretKey = (TextView) view.findViewById(R.id.lbl_secret_key);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showCredentials:
                showCredentials();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCredentials() {
        AWSCredentials credentials = ((MainActivity) getActivity()).getClientManager().getCredentials();
        mLabelAccessKey.setText(String.format("%s\n%s", getString(R.string.lbl_access_key), credentials.getAWSAccessKeyId()));
        mLabelSecretKey.setText(String.format("%s\n%s", getString(R.string.lbl_secret_key), credentials.getAWSSecretKey()));
    }
*/
}
