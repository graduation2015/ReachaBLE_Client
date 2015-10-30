package jp.ac.it_college.std.reachable_client;


import android.content.Context;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class AWSClientManager {

    private AWSCredentials mCredentials;
    private AmazonS3Client s3Client;
    private TransferUtility transferUtility;

    public AWSClientManager(AWSCredentials credentials) {
        this.mCredentials = credentials;
    }

    private AmazonS3Client makeS3Client() {
        s3Client = new AmazonS3Client(mCredentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_NORTHEAST_1));
        return s3Client;
    }

    public AmazonS3Client getS3Client() {
        if (s3Client == null) {
            s3Client = makeS3Client();
        }

        return s3Client;
    }

    private TransferUtility makeTransferUtility(Context context) {
        //Download and Upload用のUtilityセットアップ
        TransferUtility transferUtility = new TransferUtility(getS3Client(), context);
        return  transferUtility;
    }

    public TransferUtility getTransferUtility(Context context) {
        if (transferUtility == null) {
            transferUtility = makeTransferUtility(context);
        }

        return transferUtility;
    }

    public AWSCredentials getCredentials() {
        return mCredentials;
    }
}