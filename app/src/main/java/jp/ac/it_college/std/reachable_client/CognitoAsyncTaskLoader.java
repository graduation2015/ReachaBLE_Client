package jp.ac.it_college.std.reachable_client;
import android.content.AsyncTaskLoader;
import android.content.Context;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class CognitoAsyncTaskLoader extends AsyncTaskLoader<AWSCredentials>{
    private Context mContext;

    public CognitoAsyncTaskLoader(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public AWSCredentials loadInBackground() {
        CognitoCachingCredentialsProvider provider = new CognitoCachingCredentialsProvider(
                mContext,
                Constants.IDENTITY_POOL_ID,
                Regions.AP_NORTHEAST_1
        );

        return provider.getCredentials();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}