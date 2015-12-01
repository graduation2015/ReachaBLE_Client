package jp.ac.it_college.std.reachable_client;

import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

class S3DownloadListener implements TransferListener {
    private static final String DIALOG_TITLE = "Download";
    private static final String DIALOG_MESSAGE = "Downloading...";
    private static final String TAG = "S3DownloadListener";

    @Override
    public void onStateChanged(int i, TransferState transferState) {
        Log.v("test", transferState.toString());
        switch (transferState) {
            case IN_PROGRESS:
                break;
            case COMPLETED:
                break;
            case FAILED:
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