package jp.ac.it_college.std.reachable_client;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.amazonaws.auth.AWSCredentials;

import jp.ac.it_college.std.reachable_client.aws.AWSClientManager;

public class MainActivity extends AppCompatActivity
            implements LoaderManager.LoaderCallbacks<AWSCredentials> {


    private String[] mPlanetTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private AWSClientManager mClientManager;
    private ProgressDialogFragment mDialogFragment;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragment(savedInstanceState);
        findViews();
        setUpToolbar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.home:
                fragment = new MainFragment();
                getFragmentManager().beginTransaction().replace(R.id.container_content, fragment).commit();
                break;
            case R.id.switchForActionBar:
                Log.v("test", "switch touched");
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void findViews() {
        mPlanetTitles = getResources().getStringArray(R.array.categories);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
    }


    private void setUpDrawerList() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar
                ,R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //ドロワーを開いた時に呼ばれる
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //ドロワーを閉じた時に呼ばれる
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_content, new MainFragment())
                    .commit();
        }
        mDialogFragment = new ProgressDialogFragment().newInstance("Credentials", "Getting credentials");
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public AWSClientManager getClientManager() {
        return mClientManager;
    }

    /* Implemented LoaderManager.LoaderCallbacks */
    @Override
    public Loader<AWSCredentials> onCreateLoader(int i, Bundle bundle) {
        mDialogFragment.show(getFragmentManager(), "Credentials");
        return new CognitoAsyncTaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AWSCredentials> loader, AWSCredentials awsCredentials) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDialogFragment.dismiss();
            }
        });

        mClientManager = new AWSClientManager(awsCredentials);
    }

    @Override
    public void onLoaderReset(Loader<AWSCredentials> loader) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int backStackCnt = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCnt != 0) {
            getSupportFragmentManager().popBackStack();
        }
    }
}