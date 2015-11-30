package jp.ac.it_college.std.reachable_client;
import android.app.Fragment;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.auth.AWSCredentials;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity
            implements LoaderManager.LoaderCallbacks<AWSCredentials> {


    private String[] mPlanetTitles;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private AWSClientManager mClientManager;
    private ProgressDialogFragment mDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragment(savedInstanceState);
        findViews();
        setUpDrawerList();
        setUpToolbar();
        initAWSClient();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setUpDrawerList() {
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
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void initAWSClient() {
        getLoaderManager().restartLoader(0, null, this);
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

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        public static final int FRAGMENT_MAIN = 0;

        //TODO:フィルター
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectItem(i);
        }

        private void selectItem(int position) {
            Fragment fragment = getFragment(position);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_content, fragment)
                    .commit();

            mDrawerList.setItemChecked(position, true);
            setTitle(mPlanetTitles[position]);
            mDrawerLayout.closeDrawers();
        }

        private Fragment getFragment(int position) {
            switch (position) {
                case FRAGMENT_MAIN:
                    return new MainFragment();
                default:
                    return new MainFragment();
            }
        }
    }

}